package com.ticket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 
 * 基于 Redis 实现分布式锁，支持：
 * 1. 可重入锁（基于 ThreadLocal）
 * 2. 自动释放（锁超时自动释放，防止死锁）
 * 3. 安全释放（只有锁的持有者才能释放锁，使用 Lua 脚本保证原子性）
 * 4. 异常安全（在 finally 块中释放锁）
 * 
 * 使用示例：
 * <pre>
 * @Autowired
 * private DistributedLock distributedLock;
 * 
 * public void someMethod() {
 *     String lockKey = "lock:order:create:" + eventId;
 *     if (distributedLock.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
 *         try {
 *             // 业务逻辑
 *         } finally {
 *             distributedLock.unlock(lockKey);
 *         }
 *     } else {
 *         // 获取锁失败
 *     }
 * }
 * </pre>
 */
@Component
public class DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedLock.class);
    
    private final StringRedisTemplate stringRedisTemplate;
    
    // 存储当前线程持有的锁信息（用于可重入）
    // Key: lockKey, Value: lockValue (UUID)
    private static final ThreadLocal<String> LOCK_VALUE_HOLDER = new ThreadLocal<>();
    
    // Lua 脚本：释放锁（只有锁的持有者才能释放）
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    private final DefaultRedisScript<Long> unlockScript;
    
    public DistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_SCRIPT);
        this.unlockScript.setResultType(Long.class);
    }
    
    /**
     * 尝试获取锁（非阻塞）
     * 
     * @param lockKey 锁的 Key
     * @param timeout 锁的持有时间（超时自动释放）
     * @param unit 时间单位
     * @return true 表示获取成功，false 表示获取失败
     */
    public boolean tryLock(String lockKey, long timeout, TimeUnit unit) {
        try {
            // 检查是否已经持有锁（可重入）
            String existingValue = LOCK_VALUE_HOLDER.get();
            if (existingValue != null && stringRedisTemplate.hasKey(lockKey)) {
                String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
                if (existingValue.equals(currentValue)) {
                    // 同一线程重复获取同一把锁，直接返回成功（可重入）
                    log.debug("可重入锁获取成功, lockKey={}", lockKey);
                    return true;
                }
            }
            
            // 生成唯一的锁值（用于标识锁的持有者）
            String lockValue = UUID.randomUUID().toString();
            
            // 使用 SET NX EX 命令原子性地获取锁
            // NX: 只有当 key 不存在时才设置
            // EX: 设置过期时间（秒）
            Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, timeout, unit);
            
            if (Boolean.TRUE.equals(success)) {
                // 获取锁成功，保存锁值到 ThreadLocal
                LOCK_VALUE_HOLDER.set(lockValue);
                log.debug("分布式锁获取成功, lockKey={}, lockValue={}, timeout={}{}", 
                    lockKey, lockValue, timeout, unit);
                return true;
            } else {
                log.debug("分布式锁获取失败（锁已被占用）, lockKey={}", lockKey);
                return false;
            }
        } catch (Exception e) {
            log.error("获取分布式锁异常, lockKey={}, error={}", lockKey, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 尝试获取锁（阻塞式，带超时等待）
     * 
     * @param lockKey 锁的 Key
     * @param waitTime 等待获取锁的最大时间
     * @param lockTimeout 锁的持有时间（超时自动释放）
     * @param unit 时间单位
     * @return true 表示获取成功，false 表示超时未获取到
     */
    public boolean tryLockWithWait(String lockKey, long waitTime, long lockTimeout, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(waitTime);
        
        // 在等待时间内循环尝试获取锁
        while (System.currentTimeMillis() < endTime) {
            if (tryLock(lockKey, lockTimeout, unit)) {
                return true;
            }
            
            // 短暂休眠后重试（避免 CPU 空转）
            try {
                Thread.sleep(50); // 50ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("等待获取锁时被中断, lockKey={}", lockKey);
                return false;
            }
        }
        
        log.debug("等待获取锁超时, lockKey={}, waitTime={}{}", lockKey, waitTime, unit);
        return false;
    }
    
    /**
     * 释放锁
     * 
     * 使用 Lua 脚本保证原子性：
     * 1. 检查锁的值是否匹配（只有锁的持有者才能释放）
     * 2. 如果匹配，删除锁
     * 
     * @param lockKey 锁的 Key
     */
    public void unlock(String lockKey) {
        try {
            String lockValue = LOCK_VALUE_HOLDER.get();
            if (lockValue == null) {
                log.warn("尝试释放锁但 ThreadLocal 中没有锁值, lockKey={}", lockKey);
                return;
            }
            
            // 使用 Lua 脚本原子性地释放锁
            Long result = stringRedisTemplate.execute(
                unlockScript,
                Collections.singletonList(lockKey),
                lockValue
            );
            
            if (result != null && result > 0) {
                log.debug("分布式锁释放成功, lockKey={}, lockValue={}", lockKey, lockValue);
            } else {
                log.warn("分布式锁释放失败（锁已过期或被其他线程释放）, lockKey={}, lockValue={}", 
                    lockKey, lockValue);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常, lockKey={}, error={}", lockKey, e.getMessage(), e);
        } finally {
            // 清除 ThreadLocal（防止内存泄漏）
            LOCK_VALUE_HOLDER.remove();
        }
    }
    
    /**
     * 检查当前线程是否持有指定的锁
     * 
     * @param lockKey 锁的 Key
     * @return true 表示持有锁，false 表示未持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        String lockValue = LOCK_VALUE_HOLDER.get();
        if (lockValue == null) {
            return false;
        }
        
        String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
        return lockValue.equals(currentValue);
    }
}







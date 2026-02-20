package com.ticket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式锁工具类（自定义实现 - 保留用于学习参考）
 * 
 * ⚠️ 注意：此实现已保留用于学习目的，实际生产环境建议使用 RedissonLockUtil
 * 
 * 基于 Redis 实现分布式锁，支持：
 * 1. 可重入锁（支持同一线程多次获取同一把锁，带计数）
 * 2. 多锁支持（同一线程可以持有多个不同的锁）
 * 3. 自动释放（锁超时自动释放，防止死锁）
 * 4. 锁续期（Watchdog 机制，自动延长锁的过期时间）
 * 5. 安全释放（只有锁的持有者才能释放锁，使用 Lua 脚本保证原子性）
 * 6. 异常安全（在 finally 块中释放锁）
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
 * 
 * 学习价值：
 * - 可以查看完整的实现代码，理解分布式锁的原理
 * - 了解 Lua 脚本在分布式锁中的应用
 * - 学习 Watchdog 机制的实现方式
 * - 理解可重入锁的实现原理
 * 
 * 推荐使用：
 * - 生产环境：使用 RedissonLockUtil（更稳定、功能更完善）
 * - 学习研究：可以查看此类的实现代码
 */
@Component
public class DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedLock.class);
    
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 锁信息类：存储锁的值、重入次数和续期任务
     */
    private static class LockInfo {
        final String lockValue;  // 锁的值（UUID）
        final AtomicInteger reentrantCount;  // 重入次数
        volatile ScheduledFuture<?> renewTask;  // 续期任务（用于取消）
        
        LockInfo(String lockValue) {
            this.lockValue = lockValue;
            this.reentrantCount = new AtomicInteger(1);
        }
        
        int incrementAndGet() {
            return reentrantCount.incrementAndGet();
        }
        
        int decrementAndGet() {
            return reentrantCount.decrementAndGet();
        }
        
        int getCount() {
            return reentrantCount.get();
        }
        
        void cancelRenewTask() {
            ScheduledFuture<?> task = renewTask;
            if (task != null && !task.isCancelled()) {
                task.cancel(false);
            }
        }
    }
    
    /**
     * 存储当前线程持有的所有锁信息
     * Key: lockKey, Value: LockInfo（包含 lockValue 和重入次数）
     */
    private static final ThreadLocal<Map<String, LockInfo>> LOCK_HOLDER = 
        ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    /**
     * 锁续期任务调度器（Watchdog）
     * 用于自动延长锁的过期时间
     */
    private static final ScheduledExecutorService WATCHDOG_EXECUTOR = 
        Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "distributed-lock-watchdog");
            t.setDaemon(true);
            return t;
        });
    
    // Lua 脚本：释放锁（只有锁的持有者才能释放）
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    // Lua 脚本：续期锁（只有锁的持有者才能续期）
    private static final String RENEW_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('pexpire', KEYS[1], ARGV[2]) " +
        "else " +
        "    return 0 " +
        "end";
    
    private final DefaultRedisScript<Long> unlockScript;
    private final DefaultRedisScript<Long> renewScript;
    
    public DistributedLock(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        
        // 初始化解锁脚本
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_SCRIPT);
        this.unlockScript.setResultType(Long.class);
        
        // 初始化续期脚本
        this.renewScript = new DefaultRedisScript<>();
        this.renewScript.setScriptText(RENEW_SCRIPT);
        this.renewScript.setResultType(Long.class);
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
            Map<String, LockInfo> lockMap = LOCK_HOLDER.get();
            LockInfo existingLock = lockMap.get(lockKey);
            
            // 检查是否已经持有该锁（可重入）
            if (existingLock != null) {
                // 验证 Redis 中的锁是否仍然有效
                String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
                if (existingLock.lockValue.equals(currentValue)) {
                    // 可重入：增加计数
                    int count = existingLock.incrementAndGet();
                    log.debug("可重入锁获取成功, lockKey={}, reentrantCount={}", lockKey, count);
                    return true;
                } else {
                    // 锁的值不匹配，说明锁已被其他线程持有，清除本地记录
                    log.warn("锁已被其他线程持有, lockKey={}, expected={}, actual={}", 
                        lockKey, existingLock.lockValue, currentValue);
                    lockMap.remove(lockKey);
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
                // 获取锁成功，保存锁信息到 ThreadLocal
                LockInfo lockInfo = new LockInfo(lockValue);
                lockMap.put(lockKey, lockInfo);
                
                // 启动锁续期任务（Watchdog）
                startWatchdog(lockInfo, lockKey, lockValue, timeout, unit);
                
                log.debug("分布式锁获取成功, lockKey={}, lockValue={}, timeout={}{}", 
                    lockKey, lockValue, timeout, unit);
                return true;
            } else {
                log.debug("分布式锁获取失败（锁已被占用）, lockKey={}", lockKey);
                return false;
            }
        } catch (Exception e) {
            log.error("获取分布式锁异常, lockKey={}, error={}", lockKey, e.getMessage(), e);
            // 异常时清理本地状态
            LOCK_HOLDER.get().remove(lockKey);
            throw new RuntimeException("获取分布式锁失败: " + lockKey, e);
        }
    }
    
    /**
     * 启动锁续期任务（Watchdog 机制）
     * 每隔过期时间的 1/3 时间续期一次
     * 
     * @param lockInfo 锁信息对象
     * @param lockKey 锁的 Key
     * @param lockValue 锁的值
     * @param timeout 锁的过期时间
     * @param unit 时间单位
     */
    private void startWatchdog(LockInfo lockInfo, String lockKey, String lockValue, long timeout, TimeUnit unit) {
        long timeoutMillis = unit.toMillis(timeout);
        long renewInterval = timeoutMillis / 3; // 续期间隔：过期时间的 1/3
        
        ScheduledFuture<?> task = WATCHDOG_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                Map<String, LockInfo> lockMap = LOCK_HOLDER.get();
                LockInfo currentLockInfo = lockMap.get(lockKey);
                
                // 检查锁是否仍然被当前线程持有
                if (currentLockInfo == null || !currentLockInfo.lockValue.equals(lockValue)) {
                    // 锁已被释放，停止续期
                    return;
                }
                
                // 续期锁
                Long result = stringRedisTemplate.execute(
                    renewScript,
                    Collections.singletonList(lockKey),
                    lockValue,
                    String.valueOf(timeoutMillis)
                );
                
                if (result != null && result > 0) {
                    log.debug("锁续期成功, lockKey={}, lockValue={}", lockKey, lockValue);
                } else {
                    log.warn("锁续期失败（锁已过期或被其他线程释放）, lockKey={}, lockValue={}", 
                        lockKey, lockValue);
                    // 续期失败，清除本地记录并取消续期任务
                    lockMap.remove(lockKey);
                    lockInfo.cancelRenewTask();
                }
            } catch (Exception e) {
                log.error("锁续期异常, lockKey={}, error={}", lockKey, e.getMessage(), e);
            }
        }, renewInterval, renewInterval, TimeUnit.MILLISECONDS);
        
        // 保存续期任务引用，以便后续取消
        lockInfo.renewTask = task;
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
        long sleepTime = 10; // 初始休眠时间 10ms
        final long maxSleepTime = 200; // 最大休眠时间 200ms
        
        // 在等待时间内循环尝试获取锁
        while (System.currentTimeMillis() < endTime) {
            if (tryLock(lockKey, lockTimeout, unit)) {
                return true;
            }
            
            // 指数退避策略：休眠时间逐渐增加
            try {
                Thread.sleep(sleepTime);
                // 指数退避：10ms -> 20ms -> 50ms -> 100ms -> 200ms（最大）
                sleepTime = Math.min(sleepTime * 2, maxSleepTime);
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
     * 支持可重入：只有重入次数为 0 时才真正释放锁
     * 
     * @param lockKey 锁的 Key
     * @throws IllegalStateException 如果尝试释放未持有的锁
     */
    public void unlock(String lockKey) {
        Map<String, LockInfo> lockMap = LOCK_HOLDER.get();
        LockInfo lockInfo = lockMap.get(lockKey);
        
        if (lockInfo == null) {
            log.warn("尝试释放锁但当前线程未持有该锁, lockKey={}", lockKey);
            throw new IllegalStateException("尝试释放未持有的锁: " + lockKey);
        }
        
        try {
            // 减少重入次数
            int count = lockInfo.decrementAndGet();
            
            if (count > 0) {
                // 还有重入，不释放锁
                log.debug("可重入锁释放（计数减1）, lockKey={}, remainingCount={}", lockKey, count);
                return;
            }
            
            // 重入次数为 0，取消续期任务
            lockInfo.cancelRenewTask();
            
            // 真正释放锁
            String lockValue = lockInfo.lockValue;
            
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
                throw new IllegalStateException("释放锁失败，锁可能已被其他线程释放: " + lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常, lockKey={}, error={}", lockKey, e.getMessage(), e);
            throw new RuntimeException("释放分布式锁失败: " + lockKey, e);
        } finally {
            // 从 ThreadLocal 中移除锁信息
            lockMap.remove(lockKey);
            
            // 如果当前线程没有持有任何锁，清理 ThreadLocal
            if (lockMap.isEmpty()) {
                LOCK_HOLDER.remove();
            }
        }
    }
    
    /**
     * 检查当前线程是否持有指定的锁
     * 
     * @param lockKey 锁的 Key
     * @return true 表示持有锁，false 表示未持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        Map<String, LockInfo> lockMap = LOCK_HOLDER.get();
        LockInfo lockInfo = lockMap.get(lockKey);
        
        if (lockInfo == null) {
            return false;
        }
        
        // 验证 Redis 中的锁是否仍然有效
        String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
        return lockInfo.lockValue.equals(currentValue);
    }
    
    /**
     * 获取锁的重入次数
     * 
     * @param lockKey 锁的 Key
     * @return 重入次数，如果未持有该锁则返回 0
     */
    public int getReentrantCount(String lockKey) {
        Map<String, LockInfo> lockMap = LOCK_HOLDER.get();
        LockInfo lockInfo = lockMap.get(lockKey);
        return lockInfo != null ? lockInfo.getCount() : 0;
    }
}

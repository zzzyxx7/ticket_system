package com.ticket.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 
 * 提供常用的 Redis 操作方法，统一处理序列化、反序列化、异常处理等
 */
@Component
public class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 构建缓存 Key（统一 Key 格式）
     * 
     * @param parts Key 的各个部分
     * @return 格式化的 Key，例如：buildKey("home", "events", "北京") -> "home:events:北京"
     */
    public String buildKey(String... parts) {
        return String.join(":", parts);
    }

    /**
     * 设置缓存（自动序列化为 JSON）
     * 
     * @param key 缓存 Key
     * @param value 要缓存的对象（会自动序列化为 JSON）
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param <T> 对象类型
     */
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
            log.debug("Redis set 成功, key={}, timeout={}{}", key, timeout, unit);
        } catch (Exception e) {
            // 缓存写入失败，记录日志但不影响业务（降级策略）
            log.warn("Redis set 失败, key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 设置缓存（不过期）
     * 
     * @param key 缓存 Key
     * @param value 要缓存的对象
     * @param <T> 对象类型
     */
    public <T> void set(String key, T value) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json);
            log.debug("Redis set 成功（不过期）, key={}", key);
        } catch (Exception e) {
            log.warn("Redis set 失败, key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 获取缓存（自动反序列化为对象）
     * 
     * @param key 缓存 Key
     * @param typeReference 类型引用，用于反序列化
     * @param <T> 对象类型
     * @return 缓存的对象，如果不存在或出错则返回 null
     */
    public <T> T get(String key, TypeReference<T> typeReference) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            // 缓存读取失败，记录日志但不影响业务（降级策略）
            log.warn("Redis get 失败, key={}, error={}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除缓存
     * 
     * @param key 缓存 Key
     */
    public void delete(String key) {
        try {
            stringRedisTemplate.delete(key);
            log.debug("Redis delete 成功, key={}", key);
        } catch (Exception e) {
            log.warn("Redis delete 失败, key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 判断 Key 是否存在
     * 
     * @param key 缓存 Key
     * @return true 表示存在，false 表示不存在或出错
     */
    public Boolean hasKey(String key) {
        try {
            return stringRedisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Redis hasKey 失败, key={}, error={}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 设置 Key 的过期时间
     * 
     * @param key 缓存 Key
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            stringRedisTemplate.expire(key, timeout, unit);
            log.debug("Redis expire 成功, key={}, timeout={}{}", key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis expire 失败, key={}, error={}", key, e.getMessage());
        }
    }

    /**
     * 获取 Key 的剩余过期时间（秒）
     * 
     * @param key 缓存 Key
     * @return 剩余过期时间（秒），-1 表示永不过期，-2 表示 Key 不存在
     */
    public Long getExpire(String key) {
        try {
            return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis getExpire 失败, key={}, error={}", key, e.getMessage());
            return -2L;
        }
    }
}


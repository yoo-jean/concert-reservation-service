package io.hhplus.concert.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisLockManager {

    private final StringRedisTemplate redisTemplate;

    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    /**
     * @return lock token if success, null if fail
     */
    public String tryLock(String key, long ttlMs) {
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, token, ttlMs, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    /**
     * 재시도 기반 락 획득.
     * - waitMs 동안 락 획득을 시도
     * - retryDelayMs ~ retryDelayMs*2 지수 백오프(지터 포함)
     */
    public String lockWithRetry(String key, long ttlMs, long waitMs, long retryDelayMs) {
        long deadline = System.currentTimeMillis() + waitMs;
        long delay = retryDelayMs;

        while (System.currentTimeMillis() < deadline) {
            String token = tryLock(key, ttlMs);
            if (token != null) return token;

            // 지터 포함 sleep
            long jitter = ThreadLocalRandom.current().nextLong(0, delay);
            sleepSilently(Math.min(delay + jitter, 200L)); // 과한 sleep 방지
            delay = Math.min(delay * 2, 200L);
        }
        return null;
    }

    public void unlock(String key, String token) {
        redisTemplate.execute(
                new DefaultRedisScript<>(UNLOCK_LUA, Long.class),
                List.of(key),
                token
        );
    }

    private void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}

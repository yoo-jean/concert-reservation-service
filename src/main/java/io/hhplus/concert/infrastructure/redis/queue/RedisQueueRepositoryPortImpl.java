package io.hhplus.concert.infrastructure.redis.queue;

import io.hhplus.concert.application.queue.port.out.QueueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RedisQueueRepositoryPortImpl implements QueueRepositoryPort {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_KEY = "concert:queue:waiting";
    private static final String TOKEN_PREFIX = "concert:queue:token:";
    private static final String ACTIVE_PREFIX = "concert:queue:active:";

    @Override
    public void saveWaitingToken(String token) {
        redisTemplate.opsForZSet()
                .add(WAITING_KEY, token, System.currentTimeMillis());
    }

    @Override
    public Long getRank(String token) {
        Long rank = redisTemplate.opsForZSet()
                .rank(WAITING_KEY, token);
        return rank == null ? -1 : rank + 1;
    }

    @Override
    public List<String> popTopWaiting(int count) {

        Set<String> tokens = redisTemplate.opsForZSet()
                .range(WAITING_KEY, 0, count - 1);

        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        redisTemplate.opsForZSet()
                .removeRange(WAITING_KEY, 0, count - 1);

        return new ArrayList<>(tokens);
    }

    @Override
    public void saveTokenMapping(String token, Long userId) {
        redisTemplate.opsForValue()
                .set(TOKEN_PREFIX + token,
                        userId.toString(),
                        Duration.ofMinutes(10));
    }

    @Override
    public Optional<Long> getUserIdByToken(String token) {
        String value = redisTemplate.opsForValue()
                .get(TOKEN_PREFIX + token);

        return value == null ? Optional.empty() :
                Optional.of(Long.valueOf(value));
    }

    @Override
    public void saveActiveToken(String token, Long userId) {
        redisTemplate.opsForValue()
                .set(ACTIVE_PREFIX + token,
                        userId.toString(),
                        Duration.ofMinutes(5));
    }

    @Override
    public boolean isActive(String token) {
        return redisTemplate.hasKey(ACTIVE_PREFIX + token);
    }
}
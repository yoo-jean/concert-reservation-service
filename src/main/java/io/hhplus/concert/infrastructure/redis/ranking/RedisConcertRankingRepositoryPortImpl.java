package io.hhplus.concert.infrastructure.redis.ranking;

import io.hhplus.concert.application.ranking.dto.ConcertRankingItem;
import io.hhplus.concert.application.ranking.port.out.ConcertRankingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisConcertRankingRepositoryPortImpl implements ConcertRankingRepositoryPort {

    private static final String RANKING_KEY = "concert:ranking"; // PR과 동일 :contentReference[oaicite:3]{index=3}
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void increase(String concertId, long delta) {
        redisTemplate.opsForZSet()
                .incrementScore(RANKING_KEY, concertId, delta);
    }

    @Override
    public void decrease(String concertId, long delta) {
        redisTemplate.opsForZSet()
                .incrementScore(RANKING_KEY, concertId, -delta);
    }

    @Override
    public List<ConcertRankingItem> findTopRanked(int limit) {
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        Set<ZSetOperations.TypedTuple<String>> results =
                zSetOps.reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        List<ConcertRankingItem> items = new ArrayList<>();
        if (results == null) return items;

        for (ZSetOperations.TypedTuple<String> tuple : results) {
            String id = tuple.getValue();
            long count = tuple.getScore() == null ? 0L : tuple.getScore().longValue();
            items.add(new ConcertRankingItem(id, count));
        }
        return items;
    }
}
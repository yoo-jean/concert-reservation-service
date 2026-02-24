package io.hhplus.concert.application.ranking.port.out;

import io.hhplus.concert.application.ranking.dto.ConcertRankingItem;

import java.util.List;

public interface ConcertRankingRepositoryPort {
    void increase(String concertId, long delta);
    void decrease(String concertId, long delta);
    List<ConcertRankingItem> findTopRanked(int limit);
}
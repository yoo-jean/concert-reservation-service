package io.hhplus.concert.application.ranking.service;

import io.hhplus.concert.application.ranking.dto.ConcertRankingItem;
import io.hhplus.concert.application.ranking.port.out.ConcertRankingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertRankingService {

    private final ConcertRankingRepositoryPort repository;

    public void increaseReservation(String concertId) {
        repository.increase(concertId, 1L);
    }

    public void decreaseReservation(String concertId) {
        repository.decrease(concertId, 1L);
    }

    public List<ConcertRankingItem> top(int limit) {
        return repository.findTopRanked(limit);
    }
}
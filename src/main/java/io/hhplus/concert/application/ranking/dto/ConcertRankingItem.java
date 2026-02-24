package io.hhplus.concert.application.ranking.dto;

public record ConcertRankingItem(
        String concertId,
        long reservationCount
) {}
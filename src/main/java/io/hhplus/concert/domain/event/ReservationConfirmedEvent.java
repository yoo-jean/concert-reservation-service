package io.hhplus.concert.domain.event;

public record ReservationConfirmedEvent(
        String concertId
) {}
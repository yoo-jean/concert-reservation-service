package io.hhplus.concert.application.platform.dto;

import java.time.LocalDateTime;

public record ReservationEventPayload(
        String eventType,
        Long reservationId,
        Long concertId,
        Long dateId,
        Integer seatNo,
        Long userId,
        Long paidAmount,
        LocalDateTime occurredAt
) {}
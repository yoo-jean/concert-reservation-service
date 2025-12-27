package com.example.concertreservationservice.reservationpayment.application.port.out;

import java.time.Instant;

public interface SeatPort {
    boolean tryHold(Long concertDateId, int seatNo, String userId, Instant holdUntil, Instant now);
    boolean confirmSold(Long concertDateId, int seatNo, String userId, Instant now);
    void releaseHold(Long concertDateId, int seatNo, String userId);
    long getSeatPrice(Long concertDateId, int seatNo);
}

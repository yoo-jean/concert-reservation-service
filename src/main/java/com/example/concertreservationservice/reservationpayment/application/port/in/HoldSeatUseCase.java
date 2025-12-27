package com.example.concertreservationservice.reservationpayment.application.port.in;

import java.time.Instant;

public interface HoldSeatUseCase {
    HoldSeatResult hold(HoldSeatCommand command);

    record HoldSeatCommand(String token, String userId, Long concertDateId, int seatNo) {}
    record HoldSeatResult(Instant holdUntil) {}
}

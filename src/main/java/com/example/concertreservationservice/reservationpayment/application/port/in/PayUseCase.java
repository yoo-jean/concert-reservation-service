package com.example.concertreservationservice.reservationpayment.application.port.in;

public interface PayUseCase {
    PayResult pay(PayCommand command);

    record PayCommand(String token, String userId, Long concertDateId, int seatNo, long amount) {}
    record PayResult(String status) {}
}

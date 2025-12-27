package com.example.concertreservationservice.reservationpayment.application.port.out;

public interface PaymentPort {
    void saveSuccess(String userId, Long concertDateId, int seatNo, long amount);
    void saveFail(String userId, Long concertDateId, int seatNo, long amount);
}

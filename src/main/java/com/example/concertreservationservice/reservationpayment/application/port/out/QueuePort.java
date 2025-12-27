package com.example.concertreservationservice.reservationpayment.application.port.out;

public interface QueuePort {
    boolean isActiveToken(String token, String userId);
    void expireToken(String token);
}

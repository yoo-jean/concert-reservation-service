package com.example.concertreservationservice.reservationpayment.application.port.out;

public interface PointPort {
    boolean deductIfEnough(String userId, long amount);
}


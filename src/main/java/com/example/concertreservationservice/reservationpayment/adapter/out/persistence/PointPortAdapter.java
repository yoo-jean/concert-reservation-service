package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import com.example.concertreservationservice.point.PointService;
import com.example.concertreservationservice.reservationpayment.application.port.out.PointPort;
import org.springframework.stereotype.Component;

@Component
public class PointPortAdapter implements PointPort {

    private final PointService pointService;

    public PointPortAdapter(PointService pointService) {
        this.pointService = pointService;
    }

    @Override
    public boolean deductIfEnough(String userId, long amount) {
        return pointService.deductIfEnough(userId, amount);
    }
}

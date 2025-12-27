package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import com.example.concertreservationservice.reservationpayment.application.port.out.SeatPort;
import com.example.concertreservationservice.reservationpayment.domain.SeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class SeatPersistenceAdapter implements SeatPort {

    private final SeatJpaRepository seatJpaRepository;

    public SeatPersistenceAdapter(SeatJpaRepository seatJpaRepository) {
        this.seatJpaRepository = seatJpaRepository;
    }

    @Override
    @Transactional
    public boolean tryHold(Long concertDateId, int seatNo, String userId, Instant holdUntil, Instant now) {
        int updated = seatJpaRepository.tryHold(
                concertDateId, seatNo, userId, holdUntil, now,
                SeatStatus.AVAILABLE, SeatStatus.HELD
        );
        return updated == 1;
    }

    @Override
    @Transactional
    public boolean confirmSold(Long concertDateId, int seatNo, String userId, Instant now) {
        int updated = seatJpaRepository.confirmSold(
                concertDateId, seatNo, userId, now,
                SeatStatus.HELD, SeatStatus.SOLD
        );
        return updated == 1;
    }

    @Override
    @Transactional
    public void releaseHold(Long concertDateId, int seatNo, String userId) {
        seatJpaRepository.releaseHold(concertDateId, seatNo, userId, SeatStatus.HELD, SeatStatus.AVAILABLE);
    }

    @Override
    public long getSeatPrice(Long concertDateId, int seatNo) {
        return seatJpaRepository.findByConcertDateIdAndSeatNo(concertDateId, seatNo)
                .orElseThrow(() -> new IllegalArgumentException("좌석 없음"))
                .getPrice();
    }
}

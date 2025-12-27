package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import com.example.concertreservationservice.reservationpayment.application.port.out.PaymentPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class PaymentPersistenceAdapter implements PaymentPort {

    private final PaymentJpaRepository repo;

    public PaymentPersistenceAdapter(PaymentJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void saveSuccess(String userId, Long concertDateId, int seatNo, long amount) {
        save(userId, concertDateId, seatNo, amount, "SUCCESS");
    }

    @Override
    @Transactional
    public void saveFail(String userId, Long concertDateId, int seatNo, long amount) {
        save(userId, concertDateId, seatNo, amount, "FAIL");
    }

    private void save(String userId, Long concertDateId, int seatNo, long amount, String status) {
        PaymentEntity p = new PaymentEntity();
        p.setUserId(userId);
        p.setConcertDateId(concertDateId);
        p.setSeatNo(seatNo);
        p.setAmount(amount);
        p.setStatus(status);
        p.setCreatedAt(Instant.now());
        repo.save(p);
    }
}

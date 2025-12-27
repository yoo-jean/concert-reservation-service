package com.example.concertreservationservice.query;

import com.example.concertreservationservice.query.repository.*;
import com.example.concertreservationservice.reservationpayment.adapter.out.persistence.SeatJpaRepository;
import com.example.concertreservationservice.reservationpayment.domain.SeatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ConcertQueryService {

    private final ConcertDateRepository dateRepo;
    private final SeatJpaRepository seatRepo;

    public ConcertQueryService(ConcertDateRepository dateRepo, SeatJpaRepository seatRepo) {
        this.dateRepo = dateRepo;
        this.seatRepo = seatRepo;
    }

    @Transactional(readOnly = true)
    public List<ConcertDateEntity> getDates(Long concertId) {
        return dateRepo.findAllByConcertIdOrderByDateAsc(concertId);
    }

    @Transactional(readOnly = true)
    public List<SeatView> getSeats(Long concertDateId) {
        Instant now = Instant.now();
        return seatRepo.findAllByConcertDateIdOrderBySeatNoAsc(concertDateId)
                .stream()
                .map(s -> {
                    SeatStatus st = s.getStatus();
                    if (st == SeatStatus.HELD && s.getHoldUntil() != null && s.getHoldUntil().isBefore(now)) {
                        st = SeatStatus.AVAILABLE; // ✅ 만료 홀드 = 예약 가능
                    }
                    return new SeatView(s.getSeatNo(), s.getPrice(), st.name());
                })
                .toList();
    }

    public record SeatView(int seatNo, long price, String status) {}
}

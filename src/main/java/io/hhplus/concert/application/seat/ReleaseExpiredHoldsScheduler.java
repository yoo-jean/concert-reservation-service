package io.hhplus.concert.application.seat;

import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReleaseExpiredHoldsScheduler {

    private final SeatJpaRepository seatRepo;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void run() {
        seatRepo.releaseExpired(LocalDateTime.now());
    }
}

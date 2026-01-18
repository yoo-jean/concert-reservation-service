package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class HoldSeatUseCase {

    private final SeatJpaRepository seatRepo;

    private static final int HOLD_MINUTES = 5;

    @Transactional
    public void hold(long userId, long dateId, int seatNo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);

        int updated = seatRepo.holdIfAvailable(dateId, seatNo, userId, expiresAt, now);
        if (updated != 1) {
            throw new DomainException(SEAT_HOLD_FAILED);
        }
    }
}

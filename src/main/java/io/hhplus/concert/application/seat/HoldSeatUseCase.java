package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import io.hhplus.concert.infrastructure.redis.RedisLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class HoldSeatUseCase {

    private final SeatJpaRepository seatRepo;
    private final RedisLockManager lockManager;

    private static final int HOLD_MINUTES = 5;

    private static final long LOCK_TTL_MS = 3_000;
    private static final long LOCK_WAIT_MS = 300;     // 최대 0.3초만 기다림 (과제용 적당)
    private static final long LOCK_RETRY_DELAY_MS = 20;

    @Transactional
    public void hold(long userId, long dateId, int seatNo) {
        String lockKey = buildSeatLockKey(dateId, seatNo);

        // ✅ 재시도 락 획득
        String token = lockManager.lockWithRetry(lockKey, LOCK_TTL_MS, LOCK_WAIT_MS, LOCK_RETRY_DELAY_MS);

        if (token == null) {
            throw new DomainException(SEAT_HOLD_FAILED);
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = now.plusMinutes(HOLD_MINUTES);

            int updated = seatRepo.holdIfAvailable(dateId, seatNo, userId, expiresAt, now);
            if (updated != 1) {
                throw new DomainException(SEAT_HOLD_FAILED);
            }
        } finally {
            lockManager.unlock(lockKey, token);
        }
    }

    private String buildSeatLockKey(long dateId, int seatNo) {
        return "lock:seat:" + dateId + ":" + seatNo;
    }
}

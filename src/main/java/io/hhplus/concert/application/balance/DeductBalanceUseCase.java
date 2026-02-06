package io.hhplus.concert.application.balance;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import io.hhplus.concert.infrastructure.redis.RedisLockManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class DeductBalanceUseCase {

    private final UserBalanceJpaRepository balanceRepo;
    private final RedisLockManager lockManager;


    private static final long LOCK_TTL_MS = 3_000;


    private static final long LOCK_WAIT_MS = 200;
    private static final long LOCK_RETRY_DELAY_MS = 20;

    @Transactional
    public void deduct(long userId, long amount) {

        String lockKey = buildBalanceLockKey(userId);

        // ✅ 재시도 포함 락 획득
        String token = lockManager.lockWithRetry(
                lockKey,
                LOCK_TTL_MS,
                LOCK_WAIT_MS,
                LOCK_RETRY_DELAY_MS
        );

        if (token == null) {
            throw new DomainException(NOT_ENOUGH_BALANCE);
        }

        try {
            // ===== 임계구역 시작 =====

            int updated = balanceRepo.deductIfEnough(userId, amount);

            if (updated != 1) {
                throw new DomainException(NOT_ENOUGH_BALANCE);
            }

            // ===== 임계구역 끝 =====

        } finally {
            lockManager.unlock(lockKey, token);
        }
    }

    private String buildBalanceLockKey(long userId) {
        // 평가 포인트: "유저 단위" 락
        return "lock:balance:user:" + userId;
    }
}

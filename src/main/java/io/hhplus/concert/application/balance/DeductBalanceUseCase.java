package io.hhplus.concert.application.balance;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class DeductBalanceUseCase {

    private final UserBalanceJpaRepository balanceRepo;

    @Transactional
    public void deduct(long userId, long amount) {
        int updated = balanceRepo.deductIfEnough(userId, amount);
        if (updated != 1) {
            throw new DomainException(NOT_ENOUGH_BALANCE);
        }
    }
}

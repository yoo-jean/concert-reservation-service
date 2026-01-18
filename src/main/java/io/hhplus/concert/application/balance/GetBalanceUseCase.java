package io.hhplus.concert.application.balance;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class GetBalanceUseCase {

    private final UserBalanceJpaRepository repo;

    @Transactional(readOnly = true)
    public long get(long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new DomainException(BALANCE_NOT_FOUND))
                .getBalance();
    }
}

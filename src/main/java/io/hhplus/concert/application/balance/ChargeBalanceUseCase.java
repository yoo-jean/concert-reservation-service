package io.hhplus.concert.application.balance;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaEntity;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class ChargeBalanceUseCase {

    private final UserBalanceJpaRepository repo;

    @Transactional
    public void charge(long userId, long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");

        // 없으면 생성
        repo.findById(userId).orElseGet(() -> repo.save(
                UserBalanceJpaEntity.builder().userId(userId).balance(0L).build()
        ));

        repo.add(userId, amount);
    }
}

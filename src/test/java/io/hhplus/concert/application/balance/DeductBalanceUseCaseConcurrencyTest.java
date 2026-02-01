package io.hhplus.concert.application.balance;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaEntity;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeductBalanceUseCaseConcurrencyTest {

    @Autowired DeductBalanceUseCase deductBalanceUseCase;
    @Autowired UserBalanceJpaRepository balanceRepo;

    @Test
    void deduct_concurrently_neverNegative() throws Exception {
        // given
        long userId = 1L;
        long initial = 10_000L;
        long amount = 1_000L;

        seedBalance(userId, initial);

        int threads = 30;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    deductBalanceUseCase.deduct(userId, amount);
                    success.incrementAndGet();
                } catch (DomainException e) {
                    fail.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        // then
        UserBalanceJpaEntity b = balanceRepo.findById(userId).orElseThrow();

        assertEquals(0L, b.getBalance()); // 10번 성공하면 0
        assertEquals(10, success.get());
        assertEquals(20, fail.get());
    }

    private void seedBalance(long userId, long balance) {
        balanceRepo.findById(userId).ifPresent(balanceRepo::delete);

        balanceRepo.save(
                UserBalanceJpaEntity.builder()
                        .userId(userId)
                        .balance(balance)
                        .build()
        );
    }
}

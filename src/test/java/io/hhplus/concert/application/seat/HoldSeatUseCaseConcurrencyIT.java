package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HoldSeatUseCaseConcurrencyIT {

    @Autowired
    HoldSeatUseCase holdSeatUseCase;

    @Autowired
    SeatJpaRepository seatRepo;

    private final long dateId = 1L;
    private final int seatNo = 1;

    @BeforeEach
    void setUpTestData() {
    }

    @Test
    void 동시에_100명이_같은좌석_hold하면_성공은_1건만() throws Exception {
        int threadCount = 100;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;

            pool.submit(() -> {
                try {
                    holdSeatUseCase.hold(userId, dateId, seatNo);
                    success.incrementAndGet();
                } catch (DomainException e) {
                    fail.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(success.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(threadCount - 1);
    }
}

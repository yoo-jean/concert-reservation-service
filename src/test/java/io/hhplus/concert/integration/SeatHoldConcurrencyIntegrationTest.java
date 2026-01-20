package io.hhplus.concert.integration;

import io.hhplus.concert.application.seat.HoldSeatUseCase;
import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SeatHoldConcurrencyIntegrationTest {

    @Autowired HoldSeatUseCase holdSeatUseCase;
    @Autowired SeatJpaRepository seatRepo;

    @BeforeEach
    void setup() {
        seatRepo.deleteAll();
    }

    @Test
    void concurrent_hold_only_one_success() throws Exception {
        long dateId = 300L;
        int seatNo = 1;

        seatRepo.save(SeatJpaEntity.builder()
                .dateId(dateId)
                .seatNo(seatNo)
                .status(SeatStatus.AVAILABLE)
                .build());

        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger(0);

        Runnable r1 = () -> {
            ready.countDown();
            try { start.await(); } catch (InterruptedException ignored) {}
            try {
                holdSeatUseCase.hold(1L, dateId, seatNo);
                success.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        };

        Runnable r2 = () -> {
            ready.countDown();
            try { start.await(); } catch (InterruptedException ignored) {}
            try {
                holdSeatUseCase.hold(2L, dateId, seatNo);
                success.incrementAndGet();
            } catch (Exception ignored) {
            } finally {
                done.countDown();
            }
        };

        pool.submit(r1);
        pool.submit(r2);

        ready.await();
        start.countDown();
        done.await();

        pool.shutdown();

        assertThat(success.get()).isEqualTo(1);

        var seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seat.getHeldByUserId()).isIn(1L, 2L);
    }
}

package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HoldSeatUseCaseConcurrencyTest {

    @Autowired HoldSeatUseCase holdSeatUseCase;
    @Autowired SeatJpaRepository seatRepo;

    @Test
    void sameSeat_concurrent_hold_onlyOneSucceeds() throws Exception {
        // given
        long dateId = 1L;
        int seatNo = 1;

        seedSeat(dateId, seatNo);

        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when
        for (int i = 0; i < threads; i++) {
            long userId = 1000L + i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    holdSeatUseCase.hold(userId, dateId, seatNo);
                    success.incrementAndGet();
                } catch (DomainException e) {
                    fail.incrementAndGet();
                } catch (Exception e) {
                    // 예외가 다른 타입으로 튀면 문제라서 별도 체크
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
        assertEquals(1, success.get());
        assertEquals(threads - 1, fail.get());

        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertEquals(SeatStatus.HELD, seat.getStatus());
        assertNotNull(seat.getHeldByUserId());
        assertNotNull(seat.getHoldExpiresAt());
    }

    private void seedSeat(long dateId, int seatNo) {
        seatRepo.findByDateIdAndSeatNo(dateId, seatNo).ifPresent(seatRepo::delete);

        seatRepo.save(
                SeatJpaEntity.builder()
                        .dateId(dateId)
                        .seatNo(seatNo)
                        .status(SeatStatus.AVAILABLE)
                        .heldByUserId(null)
                        .holdExpiresAt(null)
                        .build()
        );
    }
}

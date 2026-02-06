package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class HoldSeatConcurrencyTest {

    @Autowired
    HoldSeatUseCase holdSeatUseCase;

    @Autowired
    SeatJpaRepository seatRepo;

    @Autowired
    EntityManager em;

    private final long dateId = 1L;
    private final int seatNo = 1;

    @BeforeEach
    void setUp() {
        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo)
                .orElseThrow(() -> new IllegalStateException(
                        "테스트 대상 좌석 데이터가 없습니다. dateId=1, seatNo=1 좌석을 시드로 넣어주세요."
                ));

        // ✅ setter 말고 이걸 사용!
        seat.resetToAvailableForTest();

        seatRepo.saveAndFlush(seat);
        em.clear();
    }


    @Test
    void 동시에_50명이_같은좌석_hold하면_성공은_1건만() throws Exception {

        int threadCount = 50;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1;

            pool.submit(() -> {
                try {
                    holdSeatUseCase.hold(userId, dateId, seatNo);
                    success.incrementAndGet();
                } catch (DomainException e) {
                    fail.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    log.error("unexpected error", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(finished).isTrue();
        assertThat(success.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(threadCount - 1);

        // ✅ DB 결과 검증까지 (가산점)
        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seat.getHeldByUserId()).isNotNull();
        assertThat(seat.getHoldExpiresAt()).isAfter(LocalDateTime.now());
    }
}

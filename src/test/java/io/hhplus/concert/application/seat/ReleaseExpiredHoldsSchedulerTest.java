package io.hhplus.concert.application.seat;

import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReleaseExpiredHoldsSchedulerTest {

    @Autowired SeatJpaRepository seatRepo;

    @Test
    void releaseExpiredHolds_makesSeatAvailable() {
        long dateId = 2L;
        int seatNo = 1;

        seatRepo.findByDateIdAndSeatNo(dateId, seatNo).ifPresent(seatRepo::delete);

        seatRepo.save(
                SeatJpaEntity.builder()
                        .dateId(dateId)
                        .seatNo(seatNo)
                        .status(SeatStatus.HELD)
                        .heldByUserId(999L)
                        .holdExpiresAt(LocalDateTime.now().minusMinutes(10))
                        .build()
        );

        int updated = seatRepo.releaseExpired(LocalDateTime.now());
        assertEquals(1, updated);

        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertEquals(SeatStatus.AVAILABLE, seat.getStatus());
        assertNull(seat.getHeldByUserId());
        assertNull(seat.getHoldExpiresAt());
    }
}

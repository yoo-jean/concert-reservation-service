package io.hhplus.concert.integration;

import io.hhplus.concert.application.seat.HoldSeatUseCase;
import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SeatHoldExpiryIntegrationTest {

    @Autowired HoldSeatUseCase holdSeatUseCase;
    @Autowired SeatJpaRepository seatRepo;

    @BeforeEach
    void setup() {
        seatRepo.deleteAll();
    }

    @Test
    void after_expiry_other_user_can_hold() {
        long dateId = 200L;
        int seatNo = 1;

        // 좌석 준비
        seatRepo.save(SeatJpaEntity.builder()
                .dateId(dateId)
                .seatNo(seatNo)
                .status(SeatStatus.AVAILABLE)
                .build());

        // user1 HOLD
        holdSeatUseCase.hold(1L, dateId, seatNo);

        // holdExpiresAt을 과거로 조작 (만료 처리)
        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        SeatJpaEntity expired = SeatJpaEntity.builder()
                .id(seat.getId())
                .dateId(seat.getDateId())
                .seatNo(seat.getSeatNo())
                .status(seat.getStatus())
                .heldByUserId(seat.getHeldByUserId())
                .holdExpiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        seatRepo.save(expired);

        // user2가 HOLD 가능해야 함
        holdSeatUseCase.hold(2L, dateId, seatNo);

        SeatJpaEntity after = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(after.getHeldByUserId()).isEqualTo(2L);
        assertThat(after.getHoldExpiresAt()).isAfter(LocalDateTime.now());
    }
}

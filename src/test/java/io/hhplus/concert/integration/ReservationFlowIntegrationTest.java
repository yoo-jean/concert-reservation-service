package io.hhplus.concert.integration;

import io.hhplus.concert.application.payment.PayUseCase;
import io.hhplus.concert.application.seat.HoldSeatUseCase;
import io.hhplus.concert.domain.seat.SeatStatus;
import io.hhplus.concert.domain.token.QueueTokenStatus;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaEntity;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import io.hhplus.concert.infrastructure.persistence.payment.PaymentJpaRepository;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaEntity;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ReservationFlowIntegrationTest {

    @Autowired HoldSeatUseCase holdSeatUseCase;
    @Autowired PayUseCase payUseCase;

    @Autowired SeatJpaRepository seatRepo;
    @Autowired UserBalanceJpaRepository balanceRepo;
    @Autowired PaymentJpaRepository paymentRepo;
    @Autowired QueueTokenJpaRepository tokenRepo;

    @BeforeEach
    void setup() {
        paymentRepo.deleteAll();
        seatRepo.deleteAll();
        balanceRepo.deleteAll();
        tokenRepo.deleteAll();
    }

    @Test
    void token_hold_pay_flow_success() {
        long userId = 1L;
        long dateId = 100L;
        int seatNo = 1;

        // 1) ACTIVE 토큰 준비
        String token = "token-1";
        tokenRepo.save(QueueTokenJpaEntity.builder()
                .token(token)
                .userId(userId)
                .status(QueueTokenStatus.ACTIVE)
                .waitingOrder(1L)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build());

        // 2) 좌석 준비 (AVAILABLE)
        seatRepo.save(SeatJpaEntity.builder()
                .dateId(dateId)
                .seatNo(seatNo)
                .status(SeatStatus.AVAILABLE)
                .build());

        // 3) 잔액 준비
        balanceRepo.save(UserBalanceJpaEntity.builder()
                .userId(userId)
                .balance(10_000L)
                .build());

        // 4) HOLD
        holdSeatUseCase.hold(userId, dateId, seatNo);

        var heldSeat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertThat(heldSeat.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(heldSeat.getHeldByUserId()).isEqualTo(userId);

        // 5) PAY
        long payAmount = 3_000L;
        String idem = "idem-001";
        long paymentId = payUseCase.pay(token, userId, dateId, seatNo, payAmount, idem);

        assertThat(paymentId).isPositive();

        // 6) 결과 검증
        var soldSeat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo).orElseThrow();
        assertThat(soldSeat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(soldSeat.getHeldByUserId()).isEqualTo(userId);

        var balance = balanceRepo.findById(userId).orElseThrow().getBalance();
        assertThat(balance).isEqualTo(7_000L);

        assertThat(paymentRepo.findById(paymentId)).isPresent();

        var tokenEntity = tokenRepo.findById(token).orElseThrow();
        assertThat(tokenEntity.getStatus()).isEqualTo(QueueTokenStatus.EXPIRED);
    }
}

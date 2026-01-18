package io.hhplus.concert.application.payment;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.domain.token.QueueTokenStatus;
import io.hhplus.concert.infrastructure.persistence.balance.UserBalanceJpaRepository;
import io.hhplus.concert.infrastructure.persistence.payment.PaymentJpaEntity;
import io.hhplus.concert.infrastructure.persistence.payment.PaymentJpaRepository;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaEntity;
import io.hhplus.concert.infrastructure.persistence.seat.SeatJpaRepository;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class PayUseCase {

    private final SeatJpaRepository seatRepo;
    private final UserBalanceJpaRepository balanceRepo;
    private final PaymentJpaRepository paymentRepo;
    private final QueueTokenJpaRepository tokenRepo;

    @Transactional
    public long pay(String token, long userId, long dateId, int seatNo, long amount, String idempotencyKey) {
        // 1) 멱등 체크 (이미 결제 처리된 요청이면 기존 결과 반환)
        return paymentRepo.findByIdempotencyKey(idempotencyKey)
                .map(PaymentJpaEntity::getId)
                .orElseGet(() -> doPay(token, userId, dateId, seatNo, amount, idempotencyKey));
    }

    private long doPay(String token, long userId, long dateId, int seatNo, long amount, String idempotencyKey) {
        LocalDateTime now = LocalDateTime.now();

        // 2) 좌석 조회 + HELD 검증
        SeatJpaEntity seat = seatRepo.findByDateIdAndSeatNo(dateId, seatNo)
                .orElseThrow(() -> new DomainException(SEAT_NOT_FOUND));

        if (!seat.isHeldBy(userId, now)) {
            throw new DomainException(SEAT_NOT_YOURS);
        }

        // 3) 잔액 차감(조건부 UPDATE)
        int deducted = balanceRepo.deductIfEnough(userId, amount);
        if (deducted != 1) {
            throw new DomainException(NOT_ENOUGH_BALANCE);
        }

        // 4) 좌석 SOLD 처리
        seat.markSoldTo(userId);
        seatRepo.save(seat);

        // 5) 결제 내역 생성
        PaymentJpaEntity saved = paymentRepo.save(
                PaymentJpaEntity.builder()
                        .userId(userId)
                        .seatId(seat.getId())
                        .amount(amount)
                        .idempotencyKey(idempotencyKey)
                        .paidAt(now)
                        .build()
        );

        // 6) 토큰 만료 처리
        tokenRepo.updateStatusIfMatch(token, QueueTokenStatus.ACTIVE, QueueTokenStatus.EXPIRED, null);

        return saved.getId();
    }
}

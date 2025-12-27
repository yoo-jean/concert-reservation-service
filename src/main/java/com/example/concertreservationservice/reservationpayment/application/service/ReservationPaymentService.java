package com.example.concertreservationservice.reservationpayment.application.service;

import com.example.concertreservationservice.queue.QueueTokenService;
import com.example.concertreservationservice.reservationpayment.application.port.in.*;
import com.example.concertreservationservice.reservationpayment.application.port.out.*;
import com.example.concertreservationservice.reservationpayment.domain.SeatHoldPolicy;

import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class ReservationPaymentService implements HoldSeatUseCase, PayUseCase {

    private final SeatPort seatPort;
    private final PointPort pointPort;
    private final PaymentPort paymentPort;
    private final QueueTokenService queueTokenService;
    private final Clock clock;

    public ReservationPaymentService(SeatPort seatPort,
                                     PointPort pointPort,
                                     PaymentPort paymentPort,
                                     QueueTokenService queueTokenService) {
        this.seatPort = seatPort;
        this.pointPort = pointPort;
        this.paymentPort = paymentPort;
        this.queueTokenService = queueTokenService;
        this.clock = Clock.systemUTC();
    }

    @Override
    public HoldSeatResult hold(HoldSeatCommand cmd) {
        queueTokenService.validateActive(cmd.token(), cmd.userId());

        Instant now = Instant.now(clock);
        Instant holdUntil = now.plus(SeatHoldPolicy.HOLD_DURATION);

        boolean ok = seatPort.tryHold(cmd.concertDateId(), cmd.seatNo(), cmd.userId(), holdUntil, now);
        if (!ok) throw new IllegalStateException("이미 점유된 좌석입니다.");

        return new HoldSeatResult(holdUntil);
    }

    @Override
    public PayResult pay(PayCommand cmd) {
        queueTokenService.validateActive(cmd.token(), cmd.userId());

        // 가격 검증(선택): 클라가 amount를 보내도 되지만 서버 가격 조회가 더 안전
        long serverPrice = seatPort.getSeatPrice(cmd.concertDateId(), cmd.seatNo());
        if (cmd.amount() != serverPrice) throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");

        boolean deducted = pointPort.deductIfEnough(cmd.userId(), cmd.amount());
        if (!deducted) {
            paymentPort.saveFail(cmd.userId(), cmd.concertDateId(), cmd.seatNo(), cmd.amount());
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }

        boolean sold = seatPort.confirmSold(cmd.concertDateId(), cmd.seatNo(), cmd.userId(), Instant.now(clock));
        if (!sold) {
            paymentPort.saveFail(cmd.userId(), cmd.concertDateId(), cmd.seatNo(), cmd.amount());
            throw new IllegalStateException("좌석 홀드 만료 또는 유효하지 않은 요청입니다.");
        }

        paymentPort.saveSuccess(cmd.userId(), cmd.concertDateId(), cmd.seatNo(), cmd.amount());

        // ✅ 결제 완료 시 토큰 만료
        queueTokenService.expire(cmd.token());

        return new PayResult("SUCCESS");
    }
}

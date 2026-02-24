package io.hhplus.concert.application.platform;

import io.hhplus.concert.application.platform.dto.ReservationEventPayload;
import io.hhplus.concert.application.platform.port.out.ReservationDataPlatformPort;
import io.hhplus.concert.domain.reservation.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationToDataPlatformEventHandler {

    private final ReservationDataPlatformPort dataPlatformPort;

    @Async // 선택: 외부 호출이 느리면 비동기로 분리
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {

        ReservationEventPayload payload = new ReservationEventPayload(
                "RESERVATION_CONFIRMED",
                event.reservationId(),
                event.concertId(),
                event.dateId(),
                event.seatNo(),
                event.userId(),
                event.paidAmount(),
                event.confirmedAt()
        );

        try {
            dataPlatformPort.sendReservationConfirmed(payload);
            log.info("[DATA-PLATFORM] sent reservationConfirmed reservationId={}", event.reservationId());
        } catch (Exception e) {
            // 과제 수준이면 로그 + 실패 처리 정도면 충분
            // 실무라면 재시도/Dead-letter/Outbox 고려
            log.error("[DATA-PLATFORM] failed reservationConfirmed reservationId={}", event.reservationId(), e);
        }
    }
}
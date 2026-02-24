package io.hhplus.concert.application.reservation.service;

import io.hhplus.concert.domain.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ApplicationEventPublisher publisher;
    private final ReservationRepositoryPort reservationRepositoryPort; // 이미 있는 포트 사용 가정

    @Transactional
    public Long confirmReservation(Long reservationId, Long userId) {

        // 1) 예약 확정 + 저장 (트랜잭션)
        Reservation reservation = reservationRepositoryPort.confirm(reservationId, userId);
        // reservation 안에 concertId/dateId/seatNo/amount 등 있다고 가정

        // 2) 이벤트 발행 (커밋 후 핸들러에서 전송)
        publisher.publishEvent(new ReservationConfirmedEvent(
                reservation.getId(),
                reservation.getConcertId(),
                reservation.getDateId(),
                reservation.getSeatNo(),
                reservation.getUserId(),
                reservation.getPaidAmount(),
                LocalDateTime.now()
        ));

        return reservation.getId();
    }
}
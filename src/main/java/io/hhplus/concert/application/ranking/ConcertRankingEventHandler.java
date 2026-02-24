package io.hhplus.concert.application.ranking;

import io.hhplus.concert.application.ranking.service.ConcertRankingService;
import io.hhplus.concert.domain.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class ConcertRankingEventHandler {

    private final ConcertRankingService rankingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConfirmed(ReservationConfirmedEvent event) {
        rankingService.increaseReservation(event.concertId());
    }
}
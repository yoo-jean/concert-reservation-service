package io.hhplus.concert.application.platform.port.out;

import io.hhplus.concert.application.platform.dto.ReservationEventPayload;

public interface ReservationDataPlatformPort {
    void sendReservationConfirmed(ReservationEventPayload payload);
}
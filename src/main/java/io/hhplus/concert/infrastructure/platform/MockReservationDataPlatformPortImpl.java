package io.hhplus.concert.infrastructure.platform;

import io.hhplus.concert.application.platform.dto.ReservationEventPayload;
import io.hhplus.concert.application.platform.port.out.ReservationDataPlatformPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class MockReservationDataPlatformPortImpl implements ReservationDataPlatformPort {

    private final WebClient dataPlatformWebClient;

    @Value("${external.data-platform.base-url}")
    private String baseUrl;

    @Override
    public void sendReservationConfirmed(ReservationEventPayload payload) {
        dataPlatformWebClient.post()
                .uri(baseUrl + "/events/reservations") // mock endpoint
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block(); // 과제면 block OK (실무면 비동기/timeout 권장)
    }
}
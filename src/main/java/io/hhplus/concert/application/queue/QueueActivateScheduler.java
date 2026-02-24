package io.hhplus.concert.application.queue;

import io.hhplus.concert.application.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueActivateScheduler {

    private final QueueService queueService;

    @Scheduled(fixedDelay = 3000)
    public void activate() {
        queueService.activateTopUsers(50);
    }
}
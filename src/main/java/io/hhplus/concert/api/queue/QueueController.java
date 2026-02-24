package io.hhplus.concert.api.queue;

import io.hhplus.concert.application.queue.service.QueueUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/queue")
public class QueueController {

    private final QueueUseCase queueUseCase;

    @PostMapping("/enter/{userId}")
    public String enter(@PathVariable Long userId) {
        return queueUseCase.enter(userId);
    }

    @GetMapping("/rank/{token}")
    public Long rank(@PathVariable String token) {
        return queueUseCase.getMyRank(token);
    }

    @GetMapping("/validate/{token}")
    public boolean validate(@PathVariable String token) {
        return queueUseCase.validateActiveToken(token);
    }
}
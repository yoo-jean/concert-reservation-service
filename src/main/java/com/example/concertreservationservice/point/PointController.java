package com.example.concertreservationservice.point;

import com.example.concertreservationservice.queue.QueueTokenService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;
    private final QueueTokenService queueTokenService;

    public PointController(PointService pointService, QueueTokenService queueTokenService) {
        this.pointService = pointService;
        this.queueTokenService = queueTokenService;
    }

    @PostMapping("/charge")
    public void charge(@RequestHeader("X-QUEUE-TOKEN") String token,
                       @RequestBody ChargeReq req) {
        queueTokenService.validateActive(token, req.userId);
        pointService.charge(req.userId, req.amount);
    }

    @GetMapping("/{userId}")
    public long get(@RequestHeader("X-QUEUE-TOKEN") String token,
                    @PathVariable String userId) {
        queueTokenService.validateActive(token, userId);
        return pointService.getBalance(userId);
    }

    public static class ChargeReq {
        @NotBlank public String userId;
        @Min(1) public long amount;
    }
}

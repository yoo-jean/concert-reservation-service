package com.example.concertreservationservice.queue;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
public class QueueTokenController {

    private final QueueTokenService service;

    public QueueTokenController(QueueTokenService service) {
        this.service = service;
    }

    @PostMapping("/token")
    public IssueRes issue(@RequestBody IssueReq req) {
        QueueTokenEntity token = service.issue(req.userId);
        return new IssueRes(token.getToken(), token.getExpiresAt().toString());
    }

    public static class IssueReq {
        @NotBlank public String userId;
    }

    public record IssueRes(String token, String expiresAt) {}
}

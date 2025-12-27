package com.example.concertreservationservice.queue;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.UUID;

@Service
public class QueueTokenService {

    private final QueueTokenRepository repo;
    private final Clock clock;

    public QueueTokenService(QueueTokenRepository repo) {
        this.repo = repo;
        this.clock = Clock.systemUTC();
    }

    public QueueTokenEntity issue(String userId) {
        QueueTokenEntity t = new QueueTokenEntity();
        t.setToken(UUID.randomUUID().toString());
        t.setUserId(userId);
        t.setStatus(QueueTokenEntity.Status.ACTIVE);
        t.setExpiresAt(Instant.now(clock).plusSeconds(600)); // 10분
        return repo.save(t);
    }

    public void validateActive(String token, String userId) {
        QueueTokenEntity t = repo.findById(token)
                .orElseThrow(() -> new IllegalStateException("대기열 토큰 없음"));

        Instant now = Instant.now(clock);
        if (!t.getUserId().equals(userId)
                || t.getStatus() != QueueTokenEntity.Status.ACTIVE
                || t.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("대기열 검증 실패");
        }
    }

    public void expire(String token) {
        QueueTokenEntity t = repo.findById(token)
                .orElseThrow(() -> new IllegalStateException("대기열 토큰 없음"));
        t.setStatus(QueueTokenEntity.Status.EXPIRED);
        repo.save(t);
    }
}

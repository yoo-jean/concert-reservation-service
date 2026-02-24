package io.hhplus.concert.domain.queue;

import java.util.UUID;

public class QueueToken {

    private final String token;
    private final Long userId;

    private QueueToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }

    public static QueueToken issue(Long userId) {
        return new QueueToken(UUID.randomUUID().toString(), userId);
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }
}
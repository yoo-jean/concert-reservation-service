package com.example.concertreservationservice.queue;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "queue_token")
public class QueueTokenEntity {

    @Id
    private String token;

    private String userId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant expiresAt;

    public enum Status { ACTIVE, EXPIRED }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public Status getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }

    public void setToken(String token) { this.token = token; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setStatus(Status status) { this.status = status; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}

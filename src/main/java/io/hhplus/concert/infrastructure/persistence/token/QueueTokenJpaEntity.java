package io.hhplus.concert.infrastructure.persistence.token;

import io.hhplus.concert.domain.token.QueueTokenStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "queue_token",
        indexes = {
                @Index(name = "idx_queue_status", columnList = "status"),
                @Index(name = "idx_queue_user", columnList = "userId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QueueTokenJpaEntity {

    @Id
    @Column(length = 64)
    private String token; // UUID or random string

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueTokenStatus status;

    /** WAITING일 때 순번(간단 구현용) */
    @Column(nullable = false)
    private Long waitingOrder;

    /** ACTIVE 만료 시간 */
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime issuedAt;
}

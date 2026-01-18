package io.hhplus.concert.infrastructure.persistence.payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_user", columnList = "userId"),
                @Index(name = "idx_payment_seat", columnList = "seatId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_idempotency", columnNames = {"idempotencyKey"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long amount;

    /** 중복 결제 방지 키 */
    @Column(nullable = false, length = 80)
    private String idempotencyKey;

    @Column(nullable = false)
    private LocalDateTime paidAt;
}

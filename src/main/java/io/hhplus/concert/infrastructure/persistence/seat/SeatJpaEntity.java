package io.hhplus.concert.infrastructure.persistence.seat;

import io.hhplus.concert.domain.seat.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "seat",
        uniqueConstraints = @UniqueConstraint(name = "uk_seat_date_no", columnNames = {"dateId", "seatNo"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SeatJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long dateId;

    @Column(nullable = false)
    private Integer seatNo; // 1~50

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    private Long heldByUserId;
    private LocalDateTime holdExpiresAt;

    public boolean isHeldBy(Long userId, LocalDateTime now) {
        return status == SeatStatus.HELD
                && Objects.equals(heldByUserId, userId)
                && holdExpiresAt != null
                && holdExpiresAt.isAfter(now);
    }

    public void markSoldTo(Long userId) {
        this.status = SeatStatus.SOLD;
        this.heldByUserId = userId;
        this.holdExpiresAt = null;
    }

    public void resetToAvailableForTest() {
        this.status = SeatStatus.AVAILABLE;
        this.heldByUserId = null;
        this.holdExpiresAt = null;
    }

}

package io.hhplus.concert.infrastructure.persistence.balance;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_balance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserBalanceJpaEntity {

    @Id
    private Long userId;

    @Column(nullable = false)
    private Long balance;
}

package io.hhplus.concert.infrastructure.persistence.balance;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UserBalanceJpaRepository extends JpaRepository<UserBalanceJpaEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserBalanceJpaEntity b
           SET b.balance = b.balance + :amount
         WHERE b.userId = :userId
        """)
    int add(@Param("userId") Long userId, @Param("amount") long amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserBalanceJpaEntity b
           SET b.balance = b.balance - :amount
         WHERE b.userId = :userId
           AND b.balance >= :amount
        """)
    int deductIfEnough(@Param("userId") Long userId, @Param("amount") long amount);
}

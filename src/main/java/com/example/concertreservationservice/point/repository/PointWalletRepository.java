package com.example.concertreservationservice.point.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PointWalletRepository extends JpaRepository<PointWalletEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PointWalletEntity w
           set w.balance = w.balance + :amount
         where w.userId = :userId
    """)
    int charge(@Param("userId") String userId, @Param("amount") long amount);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PointWalletEntity w
           set w.balance = w.balance - :amount
         where w.userId = :userId
           and w.balance >= :amount
    """)
    int deductIfEnough(@Param("userId") String userId, @Param("amount") long amount);
}

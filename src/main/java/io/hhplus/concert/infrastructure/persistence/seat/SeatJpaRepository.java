package io.hhplus.concert.infrastructure.persistence.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;


public interface SeatJpaRepository extends JpaRepository<SeatJpaEntity, Long> {

    Optional<SeatJpaEntity> findByDateIdAndSeatNo(Long dateId, Integer seatNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
  SELECT s FROM SeatJpaEntity s
   WHERE s.dateId = :dateId AND s.seatNo = :seatNo
""")
    Optional<SeatJpaEntity> findForUpdate(@Param("dateId") Long dateId, @Param("seatNo") Integer seatNo);


    List<SeatJpaEntity> findAllByDateIdOrderBySeatNoAsc(Long dateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SeatJpaEntity s
           SET s.status = io.hhplus.concert.domain.seat.SeatStatus.HELD,
               s.heldByUserId = :userId,
               s.holdExpiresAt = :expiresAt
         WHERE s.dateId = :dateId
           AND s.seatNo = :seatNo
           AND (
                s.status = io.hhplus.concert.domain.seat.SeatStatus.AVAILABLE
                OR (s.status = io.hhplus.concert.domain.seat.SeatStatus.HELD AND s.holdExpiresAt < :now)
           )
        """)
    int holdIfAvailable(
            @Param("dateId") Long dateId,
            @Param("seatNo") Integer seatNo,
            @Param("userId") Long userId,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("now") LocalDateTime now
    );
}

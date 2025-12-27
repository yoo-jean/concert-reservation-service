package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import com.example.concertreservationservice.reservationpayment.domain.SeatStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.*;

public interface SeatJpaRepository extends JpaRepository<SeatEntity, Long> {

    Optional<SeatEntity> findByConcertDateIdAndSeatNo(Long concertDateId, int seatNo);
    List<SeatEntity> findAllByConcertDateIdOrderBySeatNoAsc(Long concertDateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SeatEntity s
           set s.status = :held,
               s.holdBy = :userId,
               s.holdUntil = :holdUntil
         where s.concertDateId = :concertDateId
           and s.seatNo = :seatNo
           and (
                s.status = :available
                or (s.status = :held and s.holdUntil < :now)
           )
    """)
    int tryHold(@Param("concertDateId") Long concertDateId,
                @Param("seatNo") int seatNo,
                @Param("userId") String userId,
                @Param("holdUntil") Instant holdUntil,
                @Param("now") Instant now,
                @Param("available") SeatStatus available,
                @Param("held") SeatStatus held);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SeatEntity s
           set s.status = :sold,
               s.ownerUserId = :userId,
               s.holdBy = null,
               s.holdUntil = null
         where s.concertDateId = :concertDateId
           and s.seatNo = :seatNo
           and s.status = :held
           and s.holdBy = :userId
           and s.holdUntil >= :now
    """)
    int confirmSold(@Param("concertDateId") Long concertDateId,
                    @Param("seatNo") int seatNo,
                    @Param("userId") String userId,
                    @Param("now") Instant now,
                    @Param("held") SeatStatus held,
                    @Param("sold") SeatStatus sold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SeatEntity s
           set s.status = :available,
               s.holdBy = null,
               s.holdUntil = null
         where s.concertDateId = :concertDateId
           and s.seatNo = :seatNo
           and s.status = :held
           and s.holdBy = :userId
    """)
    int releaseHold(@Param("concertDateId") Long concertDateId,
                    @Param("seatNo") int seatNo,
                    @Param("userId") String userId,
                    @Param("held") SeatStatus held,
                    @Param("available") SeatStatus available);
}

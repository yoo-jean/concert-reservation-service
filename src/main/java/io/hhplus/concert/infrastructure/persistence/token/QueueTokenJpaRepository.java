package io.hhplus.concert.infrastructure.persistence.token;

import io.hhplus.concert.domain.token.QueueTokenStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueTokenJpaRepository extends JpaRepository<QueueTokenJpaEntity, String> {

    Optional<QueueTokenJpaEntity> findByToken(String token);

    long countByStatus(QueueTokenStatus status);

    List<QueueTokenJpaEntity> findTop10ByStatusOrderByWaitingOrderAsc(QueueTokenStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE QueueTokenJpaEntity t
           SET t.status = :toStatus,
               t.expiresAt = :expiresAt
         WHERE t.token = :token
           AND t.status = :fromStatus
        """)
    int updateStatusIfMatch(
            @Param("token") String token,
            @Param("fromStatus") QueueTokenStatus fromStatus,
            @Param("toStatus") QueueTokenStatus toStatus,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE QueueTokenJpaEntity t
           SET t.status = io.hhplus.concert.domain.token.QueueTokenStatus.EXPIRED
         WHERE t.status = io.hhplus.concert.domain.token.QueueTokenStatus.ACTIVE
           AND t.expiresAt < :now
        """)
    int expireAllActivePast(@Param("now") LocalDateTime now);
}

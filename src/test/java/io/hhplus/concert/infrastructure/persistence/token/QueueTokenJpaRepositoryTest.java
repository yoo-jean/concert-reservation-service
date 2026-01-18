package io.hhplus.concert.infrastructure.persistence.token;

import io.hhplus.concert.domain.token.QueueTokenStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class QueueTokenJpaRepositoryTest {

    @Autowired QueueTokenJpaRepository repo;

    @Test
    void updateStatusIfMatch_updates_only_when_status_matches() {
        repo.save(QueueTokenJpaEntity.builder()
                .token("t1")
                .userId(1L)
                .status(QueueTokenStatus.WAITING)
                .waitingOrder(1L)
                .issuedAt(LocalDateTime.now())
                .build());

        int ok = repo.updateStatusIfMatch("t1", QueueTokenStatus.WAITING, QueueTokenStatus.ACTIVE,
                LocalDateTime.now().plusMinutes(10));

        int fail = repo.updateStatusIfMatch("t1", QueueTokenStatus.WAITING, QueueTokenStatus.EXPIRED,
                null);

        assertThat(ok).isEqualTo(1);
        assertThat(fail).isEqualTo(0);
        assertThat(repo.findById("t1").orElseThrow().getStatus()).isEqualTo(QueueTokenStatus.ACTIVE);
    }
}

package io.hhplus.concert.infrastructure.persistence.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class PaymentJpaRepositoryTest {

    @Autowired PaymentJpaRepository repo;

    @Test
    void can_find_by_idempotencyKey() {
        repo.save(PaymentJpaEntity.builder()
                .userId(1L)
                .seatId(10L)
                .amount(1000L)
                .idempotencyKey("idem-123")
                .paidAt(LocalDateTime.now())
                .build());

        assertThat(repo.findByIdempotencyKey("idem-123")).isPresent();
        assertThat(repo.findByIdempotencyKey("idem-xxx")).isEmpty();
    }
}

package io.hhplus.concert.infrastructure.persistence.balance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserBalanceJpaRepositoryTest {

    @Autowired UserBalanceJpaRepository repo;

    @Test
    void deductIfEnough_only_when_balance_is_enough() {
        repo.save(UserBalanceJpaEntity.builder().userId(1L).balance(1000L).build());

        int ok = repo.deductIfEnough(1L, 500L);
        int fail = repo.deductIfEnough(1L, 600L);

        assertThat(ok).isEqualTo(1);
        assertThat(fail).isEqualTo(0);
        assertThat(repo.findById(1L).orElseThrow().getBalance()).isEqualTo(500L);
    }
}

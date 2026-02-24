package io.hhplus.concert.integration;

import io.hhplus.concert.application.ranking.service.ConcertRankingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ConcertRankingIntegrationTest {

    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @BeforeAll
    static void start() {
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", redis.getMappedPort(6379).toString());
    }

    @AfterAll
    static void stop() {
        redis.stop();
    }

    @Autowired
    ConcertRankingService rankingService;

    @Test
    void ranking_increase_and_top() {
        rankingService.increaseReservation("concert-1");
        rankingService.increaseReservation("concert-1");
        rankingService.increaseReservation("concert-2");

        var top = rankingService.top(2);

        assertThat(top.get(0).concertId()).isEqualTo("concert-1");
        assertThat(top.get(0).reservationCount()).isEqualTo(2L);
    }
}
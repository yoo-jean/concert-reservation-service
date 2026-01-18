package io.hhplus.concert.infrastructure.persistence.date;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConcertDateJpaRepository extends JpaRepository<ConcertDateJpaEntity, Long> {

    List<ConcertDateJpaEntity> findAllByOpenTrueOrderByConcertDateAsc();

    Optional<ConcertDateJpaEntity> findByConcertDate(LocalDate concertDate);
}

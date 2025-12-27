package com.example.concertreservationservice.query.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcertDateRepository extends JpaRepository<ConcertDateEntity, Long> {
    List<ConcertDateEntity> findAllByConcertIdOrderByDateAsc(Long concertId);
}

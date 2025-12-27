package com.example.concertreservationservice.queue;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueTokenRepository extends JpaRepository<QueueTokenEntity, String> {}

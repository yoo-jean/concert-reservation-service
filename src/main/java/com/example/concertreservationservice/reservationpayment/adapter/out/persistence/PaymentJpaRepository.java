package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {}

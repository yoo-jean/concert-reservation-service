package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="payment")
public class PaymentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Long concertDateId;
    private int seatNo;

    private long amount;
    private String status; // SUCCESS/FAIL
    private Instant createdAt;

    public void setUserId(String userId) { this.userId = userId; }
    public void setConcertDateId(Long concertDateId) { this.concertDateId = concertDateId; }
    public void setSeatNo(int seatNo) { this.seatNo = seatNo; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

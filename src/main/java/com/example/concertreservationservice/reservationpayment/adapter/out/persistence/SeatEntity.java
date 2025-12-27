package com.example.concertreservationservice.reservationpayment.adapter.out.persistence;

import com.example.concertreservationservice.reservationpayment.domain.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "seat",
        uniqueConstraints = @UniqueConstraint(name="uk_date_seat", columnNames={"concertDateId","seatNo"}))
public class SeatEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertDateId;
    private int seatNo;               // 1~50
    private int price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    private String holdBy;            // userId
    private Instant holdUntil;

    private String ownerUserId;

    @Version
    private Long version; // 선택이지만 두면 안전성↑(낙관락)

    // getters/setters
}

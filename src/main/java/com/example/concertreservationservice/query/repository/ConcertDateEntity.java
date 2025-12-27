package com.example.concertreservationservice.query.repository;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "concert_date")
public class ConcertDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long concertId;
    private LocalDate date;

    public Long getId() { return id; }
    public Long getConcertId() { return concertId; }
    public LocalDate getDate() { return date; }

    public void setConcertId(Long concertId) { this.concertId = concertId; }
    public void setDate(LocalDate date) { this.date = date; }
}
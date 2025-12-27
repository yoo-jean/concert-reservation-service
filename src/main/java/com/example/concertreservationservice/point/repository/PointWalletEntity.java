package com.example.concertreservationservice.point.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "point_wallet")
public class PointWalletEntity {
    @Id
    private String userId;

    private long balance;

    public String getUserId() { return userId; }
    public long getBalance() { return balance; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBalance(long balance) { this.balance = balance; }
}
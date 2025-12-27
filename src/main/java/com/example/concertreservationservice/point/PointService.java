package com.example.concertreservationservice.point;

import com.example.concertreservationservice.point.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointService {
    private final PointWalletRepository repo;

    public PointService(PointWalletRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void charge(String userId, long amount) {
        repo.findById(userId).orElseGet(() -> {
            PointWalletEntity w = new PointWalletEntity();
            w.setUserId(userId);
            w.setBalance(0);
            return repo.save(w);
        });

        repo.charge(userId, amount);
    }

    @Transactional(readOnly = true)
    public long getBalance(String userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("지갑 없음"))
                .getBalance();
    }

    @Transactional
    public boolean deductIfEnough(String userId, long amount) {
        return repo.deductIfEnough(userId, amount) == 1;
    }
}

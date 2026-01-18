package io.hhplus.concert.application.queue;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.domain.token.QueueTokenStatus;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaEntity;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class IssueTokenUseCase {

    private final QueueTokenJpaRepository tokenRepo;

    @Transactional
    public String issue(long userId) {
        // 가장 단순 구현: waitingOrder = 현재 WAITING/ACTIVE 수 + 1
        long order = tokenRepo.count() + 1;

        String token = UUID.randomUUID().toString().replace("-", "");

        tokenRepo.save(QueueTokenJpaEntity.builder()
                .token(token)
                .userId(userId)
                .status(QueueTokenStatus.WAITING)
                .waitingOrder(order)
                .issuedAt(LocalDateTime.now())
                .build());

        return token;
    }
}

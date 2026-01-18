package io.hhplus.concert.application.queue;

import io.hhplus.concert.domain.error.DomainException;
import io.hhplus.concert.domain.token.QueueTokenStatus;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaEntity;
import io.hhplus.concert.infrastructure.persistence.token.QueueTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static io.hhplus.concert.domain.error.ErrorMessages.*;

@Service
@RequiredArgsConstructor
public class ValidateTokenUseCase {

    private final QueueTokenJpaRepository tokenRepo;

    @Transactional(readOnly = true)
    public QueueTokenJpaEntity validateActive(String token) {
        QueueTokenJpaEntity t = tokenRepo.findById(token)
                .orElseThrow(() -> new DomainException(TOKEN_INVALID));

        if (t.getStatus() == QueueTokenStatus.EXPIRED) {
            throw new DomainException(TOKEN_EXPIRED);
        }
        if (t.getStatus() != QueueTokenStatus.ACTIVE) {
            throw new DomainException(NOT_ACTIVE);
        }
        if (t.getExpiresAt() != null && t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new DomainException(TOKEN_EXPIRED);
        }
        return t;
    }
}

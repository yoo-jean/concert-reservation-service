package io.hhplus.concert.application.queue.service;

import io.hhplus.concert.application.queue.port.out.QueueRepositoryPort;
import io.hhplus.concert.domain.queue.QueueToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService implements QueueUseCase {

    private final QueueRepositoryPort queueRepositoryPort;

    @Override
    public String enter(Long userId) {

        QueueToken queueToken = QueueToken.issue(userId);

        queueRepositoryPort.saveWaitingToken(queueToken.getToken());
        queueRepositoryPort.saveTokenMapping(queueToken.getToken(), userId);

        return queueToken.getToken();
    }

    @Override
    public Long getMyRank(String token) {
        return queueRepositoryPort.getRank(token);
    }

    @Override
    public boolean validateActiveToken(String token) {
        return queueRepositoryPort.isActive(token);
    }

    public void activateTopUsers(int count) {

        List<String> tokens = queueRepositoryPort.popTopWaiting(count);

        tokens.forEach(token ->
                queueRepositoryPort.getUserIdByToken(token)
                        .ifPresent(userId ->
                                queueRepositoryPort.saveActiveToken(token, userId)
                        )
        );
    }
}
package io.hhplus.concert.application.queue.port.out;

import java.util.List;
import java.util.Optional;

public interface QueueRepositoryPort {

    void saveWaitingToken(String token);

    Long getRank(String token);

    List<String> popTopWaiting(int count);

    void saveTokenMapping(String token, Long userId);

    Optional<Long> getUserIdByToken(String token);

    void saveActiveToken(String token, Long userId);

    boolean isActive(String token);
}
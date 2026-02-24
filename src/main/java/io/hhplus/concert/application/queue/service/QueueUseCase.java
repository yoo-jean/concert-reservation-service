package io.hhplus.concert.application.queue.service;

public interface QueueUseCase {

    String enter(Long userId);

    Long getMyRank(String token);

    boolean validateActiveToken(String token);
}
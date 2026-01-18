package io.hhplus.concert.domain.error;

public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}

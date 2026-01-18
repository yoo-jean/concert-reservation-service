package io.hhplus.concert.domain.error;

public class ErrorMessages {
    public static final String TOKEN_INVALID = "대기열 토큰이 유효하지 않습니다.";
    public static final String TOKEN_EXPIRED = "대기열 토큰이 만료되었습니다.";
    public static final String NOT_ACTIVE = "대기열 검증에 실패했습니다. 아직 사용 가능한 상태가 아닙니다.";

    public static final String SEAT_NOT_FOUND = "좌석이 존재하지 않습니다.";
    public static final String SEAT_NOT_HELD = "좌석이 임시 배정(HELD) 상태가 아닙니다.";
    public static final String SEAT_NOT_YOURS = "해당 좌석은 현재 사용자에게 임시 배정되어 있지 않습니다.";
    public static final String SEAT_HOLD_FAILED = "이미 다른 사용자가 좌석을 선점했습니다.";

    public static final String BALANCE_NOT_FOUND = "사용자 잔액 정보가 없습니다.";
    public static final String NOT_ENOUGH_BALANCE = "잔액이 부족합니다.";

    public static final String PAYMENT_DUPLICATED = "이미 처리된 결제 요청입니다.";
}

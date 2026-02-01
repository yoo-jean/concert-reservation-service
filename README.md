#  콘서트 예약 서비스 (Concert Reservation Service)

##  프로젝트 개요

본 프로젝트는 **대기열 기반 콘서트 좌석 예약 서비스**를 구현한 과제로,\
다수의 사용자가 동시에 접근하는 환경에서도 **정합성과 동시성 안정성**을
보장하는 것을 목표로 한다.

단순 기능 구현을 넘어, - 좌석 중복 예약 방지 - 잔액 음수 방지 - 중복
결제 방지 - 대기열 기반 트래픽 제어

와 같은 **실제 서비스 환경에서 발생 가능한 문제를 구조적으로 해결**하는
데 중점을 두었다.

------------------------------------------------------------------------

##  기술 스택

-   Java 17
-   Spring Boot
-   Spring Data JPA
-   MySQL (RDBMS)
-   Gradle
-   JUnit 5
-   JUnit + 멀티스레드 테스트
-   Testcontainers (선택)

------------------------------------------------------------------------

##  아키텍처 구조

``` text
io.hhplus.concert
 ┣ domain
 ┃ ┣ seat, token, balance, payment
 ┃ ┗ 도메인 규칙 및 Repository 인터페이스
 ┣ application
 ┃ ┣ seat, payment, token, balance
 ┃ ┗ UseCase 단위 비즈니스 흐름
 ┣ infrastructure
 ┃ ┣ persistence
 ┃ ┃ ┣ seat, token, balance, payment
 ┃ ┗ JPA 구현체 (Adapter)
 ┗ api (Controller Layer)
```

-   Domain: 비즈니스 규칙 정의
-   Application: 트랜잭션 및 유스케이스 제어
-   Infrastructure: DB 접근 및 구현체

------------------------------------------------------------------------

##  주요 기능 상세

### 1. 유저 대기열 토큰 발급

-   토큰 상태: WAITING → ACTIVE → EXPIRED
-   ACTIVE 상태에서만 예약/결제 가능
-   결제 완료 시 자동 만료

------------------------------------------------------------------------

### 2. 좌석 예약 (임시 배정)

-   좌석 예약 시 HOLD 상태로 임시 배정
-   HOLD 유지 시간: 5분
-   만료 시 자동 해제

####  동시성 처리 전략

조건부 UPDATE 기반 원자적 처리

``` sql
UPDATE seat
SET status = 'HELD',
    held_by_user_id = ?,
    hold_expires_at = ?
WHERE date_id = ?
  AND seat_no = ?
  AND (
        status = 'AVAILABLE'
     OR (status = 'HELD' AND hold_expires_at < NOW())
  );
```

→ 동시에 여러 요청이 들어와도 1건만 성공

------------------------------------------------------------------------

### 3. 잔액 충전 / 차감

-   잔액 충전 기능 제공
-   결제 시 잔액 차감

####  동시성 처리 전략

``` sql
UPDATE user_balance
SET balance = balance - ?
WHERE user_id = ?
  AND balance >= ?;
```

→ 음수 잔액 및 Lost Update 방지

------------------------------------------------------------------------

### 4. 결제 API

-   좌석 HELD 상태 검증
-   잔액 차감
-   결제 내역 생성
-   좌석 SOLD 변경
-   토큰 EXPIRED 처리

####  멱등성 처리

``` sql
UNIQUE INDEX uk_payment_idempotency (idempotency_key);
```

→ 중복 결제 방지

------------------------------------------------------------------------

##  통합 테스트 구성

###  1. 전체 흐름 테스트

-   토큰 발급 → 좌석 HOLD → 결제 → 검증

###  2. 좌석 만료 테스트

-   HOLD 후 만료 시 재예약 가능 확인

###  3. 동시성 테스트

#### 좌석 예약 테스트

-   스레드: 50개
-   성공: 1 / 실패: 49

#### 잔액 차감 테스트

-   스레드: 30개
-   성공: 10 / 실패: 20

→ 동시성 정책 검증

------------------------------------------------------------------------

##  선택 과제 ① 성능 최적화

### 인덱스 설계

``` sql
-- seat
UNIQUE INDEX uk_seat_date_no (date_id, seat_no);
INDEX idx_seat_date_status (date_id, status);
INDEX idx_seat_hold_expiry (status, hold_expires_at);

-- token
INDEX idx_token_user (user_id);
INDEX idx_token_status_expiry (status, expires_at);

-- payment
UNIQUE INDEX uk_payment_idempotency (idempotency_key);
```

------------------------------------------------------------------------

##  선택 과제 ② 동시성 Rule 정리

기능        위험        대응
  ----------- ----------- ------------------
좌석 예약   중복 선점   조건부 UPDATE
잔액 차감   음수 잔액   조건부 UPDATE
결제        중복 처리   멱등키
좌석 변경   Race        PESSIMISTIC LOCK

------------------------------------------------------------------------

##  선택 과제 ③ 대기열 고도화

-   ACTIVE 최대 인원 제한
-   순차 승격 구조
-   Redis 전환 가능 구조

------------------------------------------------------------------------

##  결론

-   DB 중심 동시성 제어 구조 확립
-   테스트 기반 정책 고정
-   실서비스 수준 안정성 확보

이를 통해 **실제 서비스 환경에서도 안정적으로 동작 가능한 콘서트 예약
시스템**을 구현하였다.

------------------------------------------------------------------------

 2026.02.01 기준,

좌석 임시 배정 및 잔액 차감 로직에 대해 조건부 UPDATE 기반 동시성 제어를
적용하였으며,\
멀티스레드 테스트를 통해 중복 예약 및 음수 잔액 발생 가능성이
제거되었음을 검증하였다.

또한, 결제 멱등성 처리 및 트랜잭션 통합 설계를 통해\
다중 사용자 환경에서도 데이터 정합성과 시스템 안정성을 유지할 수 있음을
확인하였다.

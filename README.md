# 🎫 콘서트 예약 서비스 (Concert Reservation Service)

## 📌 프로젝트 개요
본 프로젝트는 **대기열 기반 콘서트 좌석 예약 서비스**를 구현한 과제로,  
다수의 사용자가 동시에 접근하는 환경에서도 **정합성과 동시성 안정성**을 보장하는 것을 목표로 한다.

단순 기능 구현을 넘어,
- 좌석 중복 예약 방지
- 잔액 음수 방지
- 중복 결제 방지
- 대기열 기반 트래픽 제어

와 같은 **실제 서비스 환경에서 발생 가능한 문제를 구조적으로 해결**하는 데 중점을 두었다.

---

## 🛠 기술 스택
- Java 17
- Spring Boot
- Spring Data JPA
- MySQL (RDBMS)
- Gradle
- JUnit 5
- Testcontainers (선택)

---

## 🧱 아키텍처 구조

```text
io.hhplus.concert
 ┣ domain
 ┃ ┣ seat, token, balance, payment
 ┃ ┗ 도메인 규칙 및 Repository 인터페이스
 ┣ application
 ┃ ┣ seat, payment, token
 ┃ ┗ UseCase 단위 비즈니스 흐름
 ┣ infrastructure
 ┃ ┣ persistence
 ┃ ┃ ┣ seat, token, balance, payment
 ┃ ┗ JPA 구현체 (Adapter)
 ┗ api (Controller Layer)
```

- **Domain Layer**: 핵심 비즈니스 규칙과 개념 정의
- **Application Layer**: 유스케이스 흐름 제어 및 트랜잭션 경계
- **Infrastructure Layer**: DB 접근, 외부 시스템 연동

---

## ✨ 주요 기능 상세

### 1️⃣ 유저 대기열 토큰 발급
- 서비스 이용을 위한 대기열 토큰 발급
- 토큰 상태: `WAITING → ACTIVE → EXPIRED`
- ACTIVE 토큰만 서비스 이용 가능
- 결제 완료 시 토큰 자동 만료

---

### 2️⃣ 예약 가능 날짜 / 좌석 조회
- 예약 가능한 날짜 목록 조회
- 특정 날짜의 좌석 목록 조회
- 좌석 번호는 1~50번으로 관리

---

### 3️⃣ 좌석 예약 요청 (임시 배정)
- 좌석 예약 시 **임시 배정(HOLD)** 처리
- HOLD 상태는 약 5분간 유지
- 만료 시간 초과 시 자동 해제되어 재예약 가능

#### 🔒 동시성 처리 전략
조건부 UPDATE 쿼리를 사용하여 **동시에 하나의 요청만 성공**하도록 보장

```sql
UPDATE seat
SET status = 'HELD',
    held_by_user_id = ?,
    hold_expires_at = ?
WHERE date_id = ?
  AND seat_no = ?
  AND (
        status = 'AVAILABLE'
     OR (status = 'HELD' AND hold_expires_at < now())
  );
```

---

### 4️⃣ 잔액 충전 / 조회
- 사용자 잔액 충전
- 결제 시 잔액 차감

#### 🔒 동시성 처리 전략
```sql
UPDATE user_balance
SET balance = balance - ?
WHERE user_id = ?
  AND balance >= ?;
```

→ 잔액 음수 및 Lost Update 방지

---

### 5️⃣ 결제 API
- 좌석 HELD 상태 검증 후 결제 수행
- 결제 성공 시:
    - 좌석 상태 → SOLD
    - 잔액 차감
    - 결제 내역 생성
    - 대기열 토큰 EXPIRED 처리

#### 🔁 멱등성 보장
- `idempotency_key`에 UNIQUE 제약
- 중복 요청 시 기존 결제 결과 반환

---

## 🧪 통합 테스트 구성 (필수 과제)

### ✅ 1. 토큰 → 좌석 예약 → 결제 전체 흐름 테스트
- 토큰 발급
- 좌석 HOLD
- 결제 성공
- 좌석 SOLD / 잔액 차감 / 토큰 만료 검증

### ✅ 2. 좌석 임시 배정 만료 테스트
- HOLD 후 만료 시간 경과
- 다른 유저가 동일 좌석 재예약 가능 여부 확인

### ✅ 3. 동시성 테스트 (좌석 중복 예약 방지)
- 동일 좌석에 대해 다중 스레드 요청
- 단 1명만 HOLD 성공하도록 검증

> 위 테스트들은 **동시성 정책을 Rule(Test)로 고정**하는 역할을 수행한다.

---

## 🧠 선택 과제 ① 성능 병목 분석 및 인덱스 설계

### 🔍 성능 병목 예상 기능
| 기능 | 원인 |
|---|---|
| 날짜별 좌석 조회 | 다수 사용자 반복 조회 |
| 좌석 HOLD | 동시 요청 집중 |
| 결제 처리 | 트랜잭션 핵심 경로 |
| 토큰 검증 | 모든 API 진입점 |

### 📈 인덱스 설계
```sql
-- 좌석
UNIQUE INDEX uk_seat_date_no (date_id, seat_no);
INDEX idx_seat_date_status (date_id, status);
INDEX idx_seat_hold_expiry (status, hold_expires_at);

-- 토큰
INDEX idx_token_user (user_id);
INDEX idx_token_status_expiry (status, expires_at);

-- 결제
UNIQUE INDEX uk_payment_idempotency (idempotency_key);
```

---

## 🧠 선택 과제 ② 동시성 민감 기능 정리 및 Rule

| 기능 | 동시성 위험 | 대응 전략 |
|---|---|---|
| 좌석 예약 | 중복 선점 | 조건부 UPDATE |
| 잔액 차감 | Lost Update | 조건부 UPDATE |
| 결제 | 중복 결제 | 멱등 키 |
| 좌석 상태 변경 | Race Condition | PESSIMISTIC_WRITE |

→ 위 정책들은 **통합 테스트로 검증 및 고정(Rule化)** 되어 있다.

---

## 🧠 선택 과제 ③ 대기열 고도화 설계

### ACTIVE 최대 N명 전략
- ACTIVE 토큰 수를 최대 N명으로 제한
- 결제 완료 시 WAITING 토큰을 순차적으로 ACTIVE 승격

**장점**
- 트래픽 제어 가능
- Redis 없이 DB 기반 구현 가능
- 실서비스 확장 시 Redis Sorted Set으로 대체 가능

---

## 🎯 결론
본 프로젝트는 다음을 중점적으로 고려하였다.

- 동시성 이슈를 사전에 식별하고 구조적으로 차단
- DB 트랜잭션과 제약조건을 활용한 정합성 보장
- 테스트를 통해 정책을 Rule로 고정

이를 통해 **실제 서비스 환경에서도 안정적으로 동작 가능한 콘서트 예약 시스템**을 구현하였다.

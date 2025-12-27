# Concert Reservation Service (콘서트 예약 서비스)

> TeamSparta 과제: **대기열 기반 콘서트 좌석 예약/결제 서비스**
> - **대기열 토큰**을 통해 *작업 가능한 사용자만* API 수행
> - 좌석 예약(홀드) 시 **일정 시간 동안 임시 배정**되어 다른 사용자가 접근 불가
> - 결제는 **사전 충전 포인트**로 진행
> - 다중 인스턴스 환경에서도 **좌석 중복 배정 방지(동시성)** 고려

---

## 1. 기술 스택

- Java 17
- Spring Boot 3.3.6
- Spring Data JPA
- H2 (in-memory)
- JUnit5 / Mockito

---

## 2. 프로젝트 구조

루트 패키지: `com.example.concertreservationservice`

- `reservationpayment/` : **Clean Architecture 적용(핵심 도메인)**
    - 좌석 홀드(임시 배정) / 결제 처리 / 결제 내역 생성
    - Port & Adapter 기반으로 외부 의존성(Mock) 분리 → 단위 테스트 용이
- `query/` : **Layered**
    - 예약 가능 날짜 조회 / 좌석 조회
- `point/` : **Layered**
    - 포인트 충전 / 조회 / 차감(원자적 차감)
- `queue/` : **Layered**
    - 대기열 토큰 발급 / 검증 / 만료

---

## 3. 핵심 설계 포인트

### 3.1 대기열 토큰 기반 접근 제어
- `POST /api/queue/token` 으로 토큰 발급
- 이후 모든 API는 요청 헤더 `X-QUEUE-TOKEN`를 받아 **토큰 검증**을 통과해야 수행 가능
- 결제 성공 시 토큰은 **만료(EXPIRED)** 처리

### 3.2 좌석 임시 배정(홀드) & 자동 해제(지연 해제)
- 좌석 예약 요청 시 좌석은 `HELD` 상태가 되며 `holdUntil`(예: 5분)까지 해당 유저에게 임시 배정
- 결제가 완료되지 않으면, `holdUntil < now` 조건에서 다른 사용자가 다시 홀드를 잡을 수 있도록 설계  
  → **배치 없이도** 조건부 UPDATE에서 만료 홀드를 처리(지연 해제)

### 3.3 동시성/다중 인스턴스 안전성
좌석 중복 배정 방지를 위해 DB **조건부 UPDATE**로 원자적 홀드 처리:

- `AVAILABLE` 이거나 `HELD` 이지만 만료된 경우에만 홀드 성공
- 여러 인스턴스에서 동시에 요청해도 **업데이트 1건만 성공** → 중복 배정 불가

핵심 쿼리 예시(SeatJpaRepository):
- `tryHold(...)`: 조건부 UPDATE
- `confirmSold(...)`: 내 홀드 + 만료 전 조건에서만 SOLD 확정

---

## 4. 실행 방법

### 4.1 빌드 & 테스트
```bash
./gradlew clean test
```

### 4.2 서버 실행
```bash
./gradlew bootRun
```

### 4.3 H2 Console
- URL: `/h2-console`
- JDBC URL: `jdbc:h2:mem:concertdb`
- User: `sa`
- Password: (빈 값)

---

## 5. API 명세

> 모든 API는 헤더 `X-QUEUE-TOKEN` 필요 (토큰 발급 API 제외)  
> 일부 조회 API는 단순화를 위해 `userId`를 query param으로 전달

---

### 5.1 대기열 토큰 발급 API (필수)

**POST** `/api/queue/token`

Request:
```json
{
  "userId": "u1"
}
```

Response:
```json
{
  "token": "xxxx-xxxx-xxxx",
  "expiresAt": "2026-01-01T00:00:00Z"
}
```

---

### 5.2 예약 가능 날짜 조회 API (필수)

**GET** `/api/concerts/{concertId}/dates?userId={userId}`

Headers:
- `X-QUEUE-TOKEN: {token}`

Response:
```json
[
  { "id": 1, "concertId": 1, "date": "2026-01-10" },
  { "id": 2, "concertId": 1, "date": "2026-01-11" }
]
```

---

### 5.3 날짜별 좌석 조회 API (필수)

**GET** `/api/concert-dates/{concertDateId}/seats?userId={userId}`

Headers:
- `X-QUEUE-TOKEN: {token}`

Response:
```json
[
  { "seatNo": 1, "price": 8000, "status": "AVAILABLE" },
  { "seatNo": 2, "price": 8000, "status": "HELD" }
]
```

> `HELD` 상태라도 `holdUntil < now`이면 응답은 `AVAILABLE`로 변환하여 반환

---

### 5.4 좌석 예약(홀드) 요청 API (필수)

**POST** `/api/seats/hold`

Headers:
- `X-QUEUE-TOKEN: {token}`

Request:
```json
{
  "userId": "u1",
  "concertDateId": 1,
  "seatNo": 10
}
```

Response:
```json
{
  "holdUntil": "2026-01-01T00:05:00Z"
}
```

---

### 5.5 포인트 충전/조회 API (필수)

#### (1) 충전
**POST** `/api/points/charge`

Headers:
- `X-QUEUE-TOKEN: {token}`

Request:
```json
{
  "userId": "u1",
  "amount": 50000
}
```

Response: `200 OK`

#### (2) 조회
**GET** `/api/points/{userId}`

Headers:
- `X-QUEUE-TOKEN: {token}`

Response:
```json
100000
```

---

### 5.6 결제 API (필수)

**POST** `/api/payments`

Headers:
- `X-QUEUE-TOKEN: {token}`

Request:
```json
{
  "userId": "u1",
  "concertDateId": 1,
  "seatNo": 10,
  "amount": 8000
}
```

Response:
```json
{
  "status": "SUCCESS"
}
```

> 결제 성공 시:
> - 포인트 원자적 차감 성공
> - 좌석 소유권 `SOLD` 확정
> - 결제 내역 생성
> - 대기열 토큰 만료 처리

---

## 6. 초기 데이터 (data.sql)

- concertId=1, 날짜 2개 생성
- 각 날짜별 좌석 1~50 생성 (price=8000, AVAILABLE)
- 테스트용 지갑: u1/u2 (balance=100000)

---

## 7. 테스트 전략

- **Clean UseCase 단위 테스트**
    - `SeatPort`, `PointPort`, `PaymentPort`, `QueueTokenService` 등 외부 의존성은 **Mock**
    - UseCase(도메인 로직)만 검증
- **Layered 서비스 단위 테스트**
    - Repository Mock으로 검증

최소 포함 테스트 예:
- 좌석 홀드 성공/실패
- 결제 성공 시 토큰 만료 및 결제 내역 저장

---

## 8. 트러블슈팅/주의 사항

- 테스트 파일은 반드시 `src/test/java` 아래에 위치해야 합니다.
- Spring Boot 플러그인 버전은 3.3.6 를 사용합니다.
- 좌석 중복 배정 방지는 DB 원자 UPDATE 기반이므로, 인스턴스가 여러 개여도 안전합니다.

---

## 9. 요청 흐름 예시(권장)

1) 토큰 발급
2) 날짜 조회
3) 좌석 조회
4) 좌석 홀드
5) 포인트 충전(필요시)
6) 결제

---


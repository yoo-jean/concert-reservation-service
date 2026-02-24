#  MSA 설계 문서 - Concert Reservation System

> 본 문서는 기존 모놀리식 콘서트 예약 시스템을 MSA(Microservice Architecture) 구조로 확장하기 위한  
> 도메인 분리 전략과 분산 트랜잭션 한계 및 해결 방안을 정의한다.

---

# 1. 목적

서비스 규모 확장에 따라 다음 요구사항이 발생하였다.

- 도메인별 독립 배포
- 트래픽 폭증 시 특정 영역만 확장
- 장애 격리(Fault Isolation)
- 팀 단위 병렬 개발

이에 따라 도메인 기반으로 배포 단위를 재설계하고,
MSA 전환 시 발생하는 트랜잭션 한계를 분석하고 해결 방안을 제시한다.

---

# 2. 도메인 분석

현재 시스템의 핵심 기능은 다음과 같다.

- 대기열 토큰 관리 (Queue)
- 좌석 홀드 및 확정 (Seat)
- 예약 생성 및 상태 관리 (Reservation)
- 잔액 차감 (Wallet/Balance)
- 결제 승인 (Payment)
- 랭킹 집계 (Ranking)
- 외부 데이터 플랫폼 전송 (Relay)

---

# 3.  제안 MSA 분리 구조

##  3.1 배포 단위 설계

| 서비스 | 주요 책임 | 데이터 저장소 |
|--------|------------|---------------|
| Queue Service | 대기열 토큰 발급/검증 | Redis |
| Seat Service | 좌석 상태 관리, 홀드/만료 | RDB |
| Reservation Service | 예약 오케스트레이션 | RDB |
| Wallet Service | 잔액 차감/환불 | RDB |
| Payment Service | 결제 승인/취소 | RDB |
| Ranking Service | 인기 공연 집계 | Redis |
| Data Relay Service | 데이터 플랫폼 전송 | RDB/Outbox |

---

## 3.2 분리 기준

- 변경 빈도 기준 분리
- 트래픽 특성 기준 분리
- 정합성 요구 수준 기준 분리
- 확장 필요성 기준 분리

Queue / Ranking 은 Redis 중심 고트래픽 도메인  
Seat / Reservation / Wallet / Payment 는 강한 정합성 요구 도메인

---

# 4. 분리로 인한 트랜잭션 한계

모놀리식 환경에서는 단일 DB 트랜잭션으로  
좌석 홀드 + 잔액 차감 + 결제 승인 + 예약 확정 처리가 가능했다.

MSA 환경에서는 각 서비스가 독립 DB를 가지므로

❌ 단일 ACID 트랜잭션 불가  
❌ 부분 실패 발생 가능  
❌ 네트워크 장애 발생 가능

예시 문제:

- 결제 성공 → 예약 실패
- 잔액 차감 성공 → 좌석 확정 실패
- 이벤트 발행 실패 → 랭킹 미반영

따라서 Strong Consistency 대신 Eventual Consistency 설계가 필요하다.

---

# 5. 해결 전략

## 5.1 Saga 패턴 (Orchestration 방식)

Reservation Service가 중앙 오케스트레이터 역할 수행

### 예약 확정 흐름

1. Queue 검증
2. Seat 홀드
3. Wallet 차감
4. Payment 승인
5. Seat 확정
6. Reservation CONFIRMED 저장

### 실패 시 보상 트랜잭션

- Payment 실패 → Wallet 환불 + Seat 홀드 해제
- Seat 확정 실패 → Payment 취소 + Wallet 환불

---

## 5.2 Outbox 패턴

문제:
DB 커밋 후 이벤트 발행 실패 시 데이터 불일치 발생

해결:
- Reservation DB에 outbox 테이블 추가
- 트랜잭션 내에서 예약 저장 + outbox 저장
- 별도 Publisher가 메시지 브로커로 전달

---

## 5.3 멱등성(Idempotency)

- Payment 승인: reservationId 기반 idempotencyKey
- Wallet 차감: ledger unique key
- 이벤트 소비자: processed_event 기록

---

# 6. 최종 아키텍처 요약

Reservation Service  
→ Seat Service  
→ Wallet Service  
→ Payment Service  
→ Reservation Confirm  
→ Outbox 저장  
→ Ranking / Relay 구독

---

# 7. 설계 결론

- 도메인별 독립 배포를 통해 확장성과 장애 격리를 확보한다.
- Saga + Outbox 조합으로 분산 트랜잭션 문제를 해결한다.
- 멱등성과 재시도 전략으로 분산 환경 안정성을 확보한다.

---

# 8. 향후 확장 전략

- Kafka 기반 이벤트 스트리밍
- Circuit Breaker 적용
- Distributed Tracing 도입
- Redis Cluster 구성
- CQRS 패턴 도입 검토

---

> 본 설계는 대량 트래픽 환경에서 확장성과 정합성을 균형 있게 유지하기 위한 MSA 전환 전략을 제시한다.

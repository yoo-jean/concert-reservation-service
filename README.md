#  Concert Reservation System

> Redis 기반 대기열(Queue) + 랭킹(Ranking) 시스템을 적용한 콘서트 예약 서비스  
> 대량 트래픽 환경에서 **DB 부하/Lock 경합을 줄이고**, **토큰 기반 접근 제어**로 예약 API를 안정적으로 운영하는 것을 목표로 설계했습니다.

---

##  1. 프로젝트 목표

인기 콘서트 예매 시 다음 문제가 발생할 수 있습니다.

- 동일 좌석에 대한 동시 예약 요청
- DB Lock 경합 증가
- 서버 과부하
- 빠른 매진 공연의 랭킹 집계 지연

이를 해결하기 위해 아래를 구현했습니다.

- ✅ Redis 기반 **대기열(Queue)** 시스템
- ✅ **토큰 기반** 접근 제어(Waiting / Active Tokens)
- ✅ TTL 기반 Active 토큰 자동 만료
- ✅ 이벤트 기반 **랭킹(Ranking)** 집계
- ✅ 스케줄러 기반 상위 N명 자동 활성화

---

## ️ 2. 패키지 구조

```
api
 ├─ ranking
 └─ queue
application
 ├─ ranking
 └─ queue
domain
 └─ queue
infrastructure
 ├─ persistence
 └─ redis
```

헥사고날 아키텍처를 기반으로 다음과 같이 분리했습니다.

- **Domain**: 비즈니스 모델(토큰 등)
- **Application**: UseCase / Service (정책, 흐름)
- **Infrastructure**: Redis/DB 구현체 (Port 구현)
- **API**: Controller (HTTP 진입점)

---

##  3. Redis 기반 대기열 설계

###  Key 설계

#### 1) Waiting Tokens (ZSET)

```
Key: concert:queue:waiting
Value: token(UUID)
Score: timestamp
```

- 먼저 들어온 순서대로 정렬(Score 기반)
- 대기 순위 계산이 빠름

#### 2) Token → User 매핑 (String)

```
Key: concert:queue:token:{token}
Value: userId
TTL: 10m
```

- 토큰 유효시간 관리(대기열에서 너무 오래된 토큰 자동 정리)

#### 3) Active Tokens (String)

```
Key: concert:queue:active:{token}
Value: userId
TTL: 5m
```

- 예약 가능 상태(Active)
- 5분 내 미사용 시 자동 만료

---

##  4. 동작 흐름

### 1) 대기열 진입(토큰 발급)

```
POST /queue/enter/{userId}
```

- UUID 토큰 발급
- ZSET(Waiting)에 토큰 등록
- token → userId 매핑 저장

### 2) 내 대기 순위 조회

```
GET /queue/rank/{token}
```

- ZSET rank 조회 후 (0-base → 1-base) 변환

### 3) 활성화 스케줄러

- 일정 주기마다 상위 N명의 Waiting 토큰을 Active로 전환

```java
@Scheduled(fixedDelay = 3000)
public void activate() {
    queueService.activateTopUsers(50);
}
```

### 4) 예약 API 접근 제어

- Active 토큰만 예약 API 접근 허용

```java
if (!queueUseCase.validateActiveToken(token)) {
    throw new IllegalStateException("대기열 통과 토큰이 아닙니다.");
}
```

---

##  5. 동시성 대응 전략

| 문제 | 해결 전략 |
|------|-----------|
| DB Lock 경합 | 대기열을 Redis로 분리하여 DB 접근 최소화 |
| 대량 트래픽 | ZSET 기반 정렬/순위로 빠른 처리 |
| 사용자 이탈 | Active/Token에 TTL 적용해 자동 정리 |
| 순위 계산 비용 | Redis ZSET rank 사용(O(logN)) |

> Redis는 단일 스레드 이벤트 루프 기반으로 동작하여, ZSET 연산이 안정적으로 처리됩니다.

---

##  6. Ranking 시스템 개요

- 예약 확정 시 `ReservationConfirmedEvent` 발행
- 이벤트 핸들러(`ConcertRankingEventHandler`)에서 Redis 랭킹 집계 저장
- `ConcertRankingController`에서 랭킹 조회 API 제공

---

##  7. 테스트 (Integration + 동시성)

> 아래 테스트는 “대기열 토큰 관리(Waiting/Active) + 스케줄러 활성화”가 정상 동작하는지 검증합니다.  
> 프로젝트에 이미 `ConcertRankingIntegrationTest.java`가 있는 것으로 보이므로, **Queue 전용 Integration Test**를 추가하거나, 기존 테스트 클래스에 섹션으로 붙여서 사용하면 됩니다.

### 7-1. Queue 토큰 발급 및 순위 검증

- 100명이 동시에 대기열에 진입했을 때, 토큰이 발급되고 rank가 정상 계산되는지 확인

```java
import io.hhplus.concert.application.queue.service.QueueUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueIntegrationTest {

    @Autowired
    QueueUseCase queueUseCase;

    @Test
    void 동시에_100명_대기열_진입_토큰발급_및_순위조회() throws Exception {
        int n = 100;

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(n);

        List<String> tokens = new CopyOnWriteArrayList<>();

        for (long i = 1; i <= n; i++) {
            long userId = i;
            executor.submit(() -> {
                try {
                    String token = queueUseCase.enter(userId);
                    tokens.add(token);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(tokens).hasSize(n);

        // 임의 토큰 하나의 rank가 -1이 아니어야 함(대기열에 존재)
        Long rank = queueUseCase.getMyRank(tokens.get(0));
        assertThat(rank).isGreaterThan(0);
    }
}
```

---

### 7-2. 활성화(Active) 전환 검증

- Waiting 상위 50명이 Active로 전환되고, Active 토큰 검증이 true가 되는지 확인
- **정확한 순서 검증 대신 “활성화된 토큰 개수=50”로 검증** (timestamp score 특성상 동시 ms 진입 시 순서가 섞일 수 있음)

```java
import io.hhplus.concert.application.queue.service.QueueService;
import io.hhplus.concert.application.queue.service.QueueUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QueueActivationIntegrationTest {

    @Autowired
    QueueUseCase queueUseCase;

    @Autowired
    QueueService queueService; // 스케줄러 대신 직접 호출용

    @Test
    void 상위50명_ACTIVE_전환_검증() {
        int n = 100;
        List<String> tokens = new ArrayList<>();

        for (long i = 1; i <= n; i++) {
            tokens.add(queueUseCase.enter(i));
        }

        // 스케줄러 대신 직접 활성화 호출
        queueService.activateTopUsers(50);

        long activeCount = tokens.stream()
                .filter(queueUseCase::validateActiveToken)
                .count();

        assertThat(activeCount).isEqualTo(50);
    }
}
```

---

### 7-3. (선택) Active 토큰 TTL 만료 검증

> TTL 테스트는 시간이 소요되므로 과제 제출에서는 “설계/설명”로 대체해도 충분합니다.  
> 만약 검증하려면 Active TTL을 짧게(예: 2초) 주입 가능한 설정으로 만들고 테스트를 구성하는 것을 권장합니다.

---

## ️ 8. 기술 스택

- Java 17
- Spring Boot
- Redis
- JPA
- Gradle

---

##  9. 설계 장점

- DB 부하 최소화(대기열/정렬/순위 계산을 Redis로 분리)
- 토큰 기반 접근 제어로 예약 API 보호
- TTL 기반 자동 정리로 운영 안정성 향상
- 헥사고날 구조로 확장/교체 용이(Port-Out)

---

##  10. 개선 가능 포인트

- Lua Script로 `popTopWaiting + saveActive` 원자 처리
- Redis Cluster 환경 대응
- WebSocket으로 실시간 대기열 UI 제공
- Redisson 기반 분산락 적용(필요 시)

---

##  11. 회고

> 단순 DB 기반 대기열 대신 Redis ZSET 기반 구조를 도입해  
> 대량 트래픽 환경에서 DB Lock 경합을 줄이고, 빠른 순위 계산이 가능하도록 설계했습니다.  
> 또한 토큰 기반 접근 제어와 TTL을 적용하여 예약 API 보호 및 사용자 이탈 상황 자동 정리를 구현했습니다.

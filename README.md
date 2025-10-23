# 포인트 관리 시스템 (TDD)

## 프로젝트 개요

사용자 포인트 충전/사용 기능을 제공하는 RESTful API 서버입니다. TDD(Test-Driven Development) 방식으로 개발되었으며, 동시성 제어 기능을 지원합니다.

## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Gradle 8.4
- JUnit 5
- Jacoco (코드 커버리지)

## 주요 요구사항

### 비즈니스 정책

**충전 정책**
- 최소 충전 금액: 100원
- 충전 단위: 10원
- 양수만 충전 가능

**사용 정책**
- 최소 사용 금액: 100원
- 잔액보다 많은 금액 사용 불가
- 양수만 사용 가능

### 동시성 제어

- 사용자별 독립적인 Lock (`ConcurrentHashMap<Long, ReentrantLock>`)
- Fair Lock을 사용하여 요청 순서 보장
- 전역 Table 접근 Lock으로 thread-safe 보장

## API 엔드포인트

### 1. 포인트 조회

```http
GET /point/{id}
```

**Response**
```json
{
  "id": 1,
  "point": 1000,
  "updateMillis": 1234567890
}
```

### 2. 포인트 이력 조회

```http
GET /point/{id}/histories
```

**Response**
```json
[
  {
    "id": 1,
    "userId": 1,
    "amount": 500,
    "type": "CHARGE",
    "updateMillis": 1234567890
  },
  {
    "id": 2,
    "userId": 1,
    "amount": 200,
    "type": "USE",
    "updateMillis": 1234567900
  }
]
```

### 3. 포인트 충전

```http
PATCH /point/{id}/charge
Content-Type: application/json

100
```

**Response**
```json
{
  "id": 1,
  "point": 1100,
  "updateMillis": 1234567890
}
```

**Error (400)**
```json
{
  "code": "400",
  "message": "최소 충전 금액은 100원 입니다."
}
```

### 4. 포인트 사용

```http
PATCH /point/{id}/use
Content-Type: application/json

100
```

**Response**
```json
{
  "id": 1,
  "point": 900,
  "updateMillis": 1234567890
}
```

**Error (400)**
```json
{
  "code": "400",
  "message": "잔액이 부족합니다."
}
```

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 코드 커버리지 리포트 생성
./gradlew test jacocoTestReport

# 애플리케이션 실행
./gradlew bootRun
```

## 테스트 커버리지

현재 코드 커버리지: **약 77.7%**

- PointServiceImpl: 100%
- UserPoint: 90.9%
- 예외 클래스: 100%

리포트 위치: `build/reports/jacoco/test/html/index.html`

---

# Java 동시성 제어 방식

## 1. synchronized 키워드

### 개념
메서드나 블록에 대한 암시적 lock을 제공하는 가장 기본적인 동기화 방법

### 장점
- 간단하고 직관적인 문법
- JVM 레벨에서 자동으로 lock 관리 (예외 발생 시 자동 해제)
- 재진입(Reentrant) 가능

### 단점
- lock 획득 실패 시 무한 대기 (타임아웃 불가)
- 읽기/쓰기 구분 없음 (모든 접근을 동일하게 처리)
- 성능 오버헤드

### 사용 사례
```java
public synchronized void deposit(long amount) {
    balance += amount;
}

// 또는 블록 단위
public void withdraw(long amount) {
    synchronized(this) {
        balance -= amount;
    }
}
```

---

## 2. ReentrantLock

### 개념
java.util.concurrent.locks 패키지의 명시적 lock 구현체

### 장점
- lock 획득 타임아웃 설정 가능 (`tryLock`)
- 공정성(Fairness) 정책 선택 가능 (FIFO 순서 보장)
- lock 획득 시도 여부 확인 가능
- 조건별 대기 큐 분리 가능 (Condition)

### 단점
- 명시적으로 unlock 필요 (finally 블록에서 처리 필수)
- synchronized보다 복잡한 코드
- 잘못 사용 시 데드락 위험

### 사용 사례
```java
private final ReentrantLock lock = new ReentrantLock(true); // fair lock

public void process() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock(); // 반드시 해제
    }
}
```

---

## 3. Concurrent Collections

### 개념
동시성을 고려하여 설계된 thread-safe 컬렉션들

### 주요 구현체
- `ConcurrentHashMap`: 분할 잠금(segment lock)을 사용하는 HashMap
- `CopyOnWriteArrayList`: 쓰기 시 복사하는 ArrayList
- `ConcurrentLinkedQueue`: lock-free 알고리즘 기반 큐

### 장점
- 명시적인 동기화 코드 불필요
- 높은 동시성 처리 성능
- 읽기 작업은 대부분 lock-free

### 단점
- 일반 컬렉션보다 메모리 사용량 높음
- CopyOnWrite 계열은 쓰기가 많으면 성능 저하

### 사용 사례
```java
private final ConcurrentHashMap<Long, User> userCache = new ConcurrentHashMap<>();

// thread-safe한 조회/수정
userCache.put(userId, user);
userCache.computeIfAbsent(userId, id -> loadUser(id));
```

---

## 4. Atomic 클래스

### 개념
CAS(Compare-And-Swap) 알고리즘 기반의 lock-free 원자적 연산 제공

### 주요 클래스
- `AtomicInteger`, `AtomicLong`: 원자적 숫자 연산
- `AtomicReference`: 객체 참조의 원자적 업데이트
- `AtomicBoolean`: 원자적 불린 연산

### 장점
- lock을 사용하지 않아 성능 우수
- 데드락 위험 없음
- 간단한 연산에 적합

### 단점
- 복잡한 비즈니스 로직에는 부적합
- CAS 실패 시 재시도로 인한 CPU 사용량 증가 가능

### 사용 사례
```java
private final AtomicLong counter = new AtomicLong(0);

public void increment() {
    counter.incrementAndGet(); // 원자적 증가
}

public long getAndAdd(long delta) {
    return counter.getAndAdd(delta);
}
```

---

## 5. ReadWriteLock

### 개념
읽기와 쓰기를 분리하여 읽기 작업 간에는 동시 접근을 허용하는 lock

### 장점
- 읽기가 많고 쓰기가 적은 경우 높은 성능
- 여러 스레드가 동시에 읽기 가능
- 데이터 일관성 보장

### 단점
- 쓰기가 많으면 일반 lock보다 오버헤드 큼
- 구현이 복잡함
- Writer starvation 가능성 (읽기 작업이 계속되면 쓰기 대기)

### 사용 사례
```java
private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();

public Data read() {
    readLock.lock();
    try {
        return data; // 여러 스레드 동시 읽기 가능
    } finally {
        readLock.unlock();
    }
}

public void write(Data newData) {
    writeLock.lock();
    try {
        this.data = newData; // 독점적 쓰기
    } finally {
        writeLock.unlock();
    }
}
```

---

## 실무 선택 가이드

| 상황 | 권장 방식 |
|------|----------|
| 간단한 메서드 동기화 | `synchronized` |
| 타임아웃이나 공정성 필요 | `ReentrantLock` |
| 단순 카운터, 플래그 | `Atomic 클래스` |
| 캐시, 공유 맵 | `ConcurrentHashMap` |
| 읽기 >> 쓰기 | `ReadWriteLock` |
| 사용자별 독립적 lock | `ConcurrentHashMap<ID, ReentrantLock>` |

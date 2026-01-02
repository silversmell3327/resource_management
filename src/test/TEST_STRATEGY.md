# 테스트를 계층별로 나누는 이유

## 테스트 피라미드 (Test Pyramid)

```
        /\
       /  \     E2E 테스트 (통합 테스트)
      /    \    - 적게, 느림, 전체 시스템
     /------\
    /        \   통합 테스트 (Integration Tests)
   /          \  - 중간, 중간 속도, 여러 컴포넌트
  /------------\
 /              \ 단위 테스트 (Unit Tests)
/----------------\ - 많음, 빠름, 단일 컴포넌트
```

## 계층별 테스트의 역할

### 1. Unit Test (단위 테스트) - Service, Repository

**예시**: `ResourceRequestServiceTest`, `ResourceRequestRepositoryTest`

**목적**: 
- 개별 메서드나 클래스의 로직을 독립적으로 검증
- 가장 빠르게 실행됨 (밀리초 단위)
- 가장 많은 테스트 케이스 작성

**특징**:
- 외부 의존성을 Mock으로 대체
- 실제 DB, 네트워크 없이 실행
- 특정 기능만 집중적으로 테스트

**장점**:
- ⚡ **빠른 실행**: 전체 테스트가 몇 초 내에 완료
- 🎯 **명확한 실패 원인**: 특정 메서드의 문제를 바로 파악
- 💰 **저렴한 비용**: 리소스 사용이 적음
- 🔄 **자주 실행**: 개발 중 실시간으로 피드백

**예시**:
```java
// Service 테스트 - 비즈니스 로직 검증
@Test
void testCreateResourceRequest_Success() {
    when(accountRepository.findById(1L))
        .thenReturn(Optional.of(testAccount));
    
    service.createResourceRequest(request);
    
    verify(repository).save(any());
}
```

---

### 2. Integration Test (통합 테스트) - Repository

**예시**: `ResourceRequestRepositoryTest`

**목적**:
- 여러 컴포넌트 간의 상호작용 검증
- 실제 JPA 매핑, 쿼리 검증
- 중간 속도로 실행

**특징**:
- 실제 인메모리 DB (H2) 사용
- Spring 컨텍스트 일부 로드
- 실제 SQL 실행

**장점**:
- 🔗 **연동 검증**: JPA 엔티티 매핑 확인
- 📊 **쿼리 검증**: 실제 생성되는 SQL 확인
- 🗄️ **DB 스키마 검증**: 테이블 구조 확인

---

### 3. Controller Test (웹 레이어 테스트)

**예시**: `ResourceRequestControllerTest`

**목적**:
- HTTP 요청/응답 검증
- JSON 직렬화/역직렬화 검증
- HTTP 상태 코드, 헤더 검증

**특징**:
- MockMvc 사용 (실제 서버 없이 HTTP 시뮬레이션)
- Service는 Mock 처리
- 빠른 실행

**장점**:
- 🌐 **API 계약 검증**: REST API 스펙 확인
- 📝 **입출력 검증**: JSON 형식 검증
- 🔒 **보안 검증**: 인증/인가 로직 테스트 가능

**예시**:
```java
@Test
void testCreateResourceRequest_Success() throws Exception {
    mockMvc.perform(post("/resource-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":1,\"message\":\"test\"}"))
            .andExpect(status().isCreated());
}
```

---

### 4. End-to-End Test (전체 시스템 테스트)

**예시**: `ResourceRequestIntegrationTest`

**목적**:
- 전체 애플리케이션의 실제 동작 검증
- Controller → Service → Repository 전체 플로우
- 가장 느리지만 실제 환경과 가장 유사

**특징**:
- 전체 Spring 컨텍스트 로드
- 실제 DB 또는 Testcontainers 사용
- 느린 실행 (초~분 단위)

**장점**:
- 🔄 **전체 플로우 검증**: 실제 사용자 시나리오 검증
- 🐛 **통합 버그 발견**: 계층 간 연동 문제 발견
- ✅ **신뢰성**: 실제 환경과 유사한 검증

**단점**:
- 🐌 **느림**: 실행 시간이 오래 걸림
- 💸 **비쌈**: 리소스 사용이 많음
- 🔧 **복잡**: 설정이 복잡함

---

## 왜 나누는가? - 핵심 이유

### 1. **빠른 피드백 루프**

```
단위 테스트:    0.1초  ✅ 즉시 피드백
Controller 테스트: 0.5초  ✅ 빠른 피드백
통합 테스트:      2초   ⚠️ 중간 피드백
E2E 테스트:       30초  ❌ 느린 피드백
```

개발 중에는 **빠른 테스트**가 필수입니다. 모든 테스트를 통합 테스트로만 작성하면:
- 한 번 실행하는데 1분 이상 소요
- 버그 수정 후 확인이 너무 느림
- 개발 생산성 저하

### 2. **명확한 책임 분리**

각 계층은 다른 책임을 가집니다:

| 계층 | 책임 | 테스트 목적 |
|------|------|------------|
| **Controller** | HTTP 요청/응답 처리 | API 스펙 검증 |
| **Service** | 비즈니스 로직 | 로직 정확성 검증 |
| **Repository** | 데이터 접근 | 데이터 저장/조회 검증 |

### 3. **격리된 테스트 (Isolation)**

```java
// ❌ 나쁜 예: 모든 것을 한 테스트에서
@Test
void testEverything() {
    // DB 설정
    // HTTP 요청
    // 비즈니스 로직
    // 검증
    // -> 하나 실패하면 전체 실패 원인 파악 어려움
}

// ✅ 좋은 예: 각각 분리
@Test
void testServiceLogic() {
    // Service만 테스트 - 로직 문제 즉시 파악
}

@Test  
void testControllerAPI() {
    // Controller만 테스트 - API 문제 즉시 파악
}
```

### 4. **유지보수성**

- **Service 로직 변경** → Service 테스트만 수정
- **API 스펙 변경** → Controller 테스트만 수정
- **DB 스키마 변경** → Repository 테스트만 수정

모두 한 곳에 있으면 하나 수정할 때마다 전체 테스트를 수정해야 함.

### 5. **비용 효율성**

```
단위 테스트 100개:  10초 소요, 리소스 적음
통합 테스트 100개:  5분 소요, DB 필요
E2E 테스트 100개:   50분 소요, 전체 인프라 필요
```

---

## 실제 개발 워크플로우

```
1. 코드 작성
   ↓
2. 단위 테스트 실행 (0.1초) ← 빠른 피드백!
   ↓ 통과
3. Controller 테스트 실행 (0.5초) ← API 확인
   ↓ 통과  
4. 통합 테스트 실행 (2초) ← 전체 연동 확인
   ↓ 통과
5. 커밋 전 전체 테스트 실행 (10초) ← 최종 확인
   ↓ 통과
6. 배포
```

---

## 언제 어떤 테스트를 쓸까?

### 단위 테스트 (Service, Repository)
- ✅ **항상**: 모든 비즈니스 로직
- ✅ 복잡한 계산 로직
- ✅ 조건문, 반복문이 많은 코드
- ✅ 에러 핸들링 로직

### Controller 테스트
- ✅ **항상**: 모든 REST API 엔드포인트
- ✅ 입력 검증 (Validation)
- ✅ 인증/인가 로직
- ✅ HTTP 상태 코드

### 통합 테스트
- ✅ 복잡한 쿼리 (JOIN, 집계 등)
- ✅ 트랜잭션 동작
- ✅ 여러 Repository 간 연동

### E2E 테스트
- ✅ 핵심 사용자 시나리오
- ✅ 중요한 비즈니스 플로우
- ⚠️ 적게 작성 (유지보수 비용이 큼)

---

## 요약

| 테스트 유형 | 개수 | 속도 | 목적 |
|-----------|------|------|------|
| 단위 테스트 | 많음 (70%) | 매우 빠름 | 로직 검증 |
| Controller 테스트 | 중간 (20%) | 빠름 | API 검증 |
| 통합 테스트 | 적음 (9%) | 중간 | 연동 검증 |
| E2E 테스트 | 매우 적음 (1%) | 느림 | 전체 플로우 |

**핵심**: 빠르고 많은 단위 테스트로 대부분의 버그를 잡고, 
필요할 때만 통합/E2E 테스트로 검증하는 것이 효율적입니다! 🎯



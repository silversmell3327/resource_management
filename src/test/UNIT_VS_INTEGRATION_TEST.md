# 단위 테스트 vs 통합 테스트 구분

## ResourceRequestStateBasedTest 분석

### 이것은 통합 테스트입니다! ❌ (단위 테스트 아님)

```java
@SpringBootTest              // ← 전체 Spring 컨텍스트 로드
@AutoConfigureMockMvc        // ← MockMvc 사용
@Transactional               // ← 트랜잭션 사용
@ActiveProfiles("test")
class ResourceRequestStateBasedTest {
    @Autowired
    private AccountRepository accountRepository;  // ← 실제 Repository
    
    @Autowired
    private ResourceRepository resourceRepository;  // ← 실제 Repository
}
```

**특징:**
- ✅ `@SpringBootTest` → 전체 Spring 컨텍스트 로드
- ✅ 실제 Repository 사용 (Mock 아님)
- ✅ 실제 DB 사용 (인메모리 H2 또는 실제 DB)
- ⚡ 실행 속도: 느림 (1-5초)
- 🎯 목적: Controller → Service → Repository 전체 플로우 검증

---

## 단위 테스트 vs 통합 테스트 비교

### 단위 테스트 (Unit Test) 예시

**파일:** `ResourceRequestServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)  // ← Spring 없음!
class ResourceRequestServiceTest {
    
    @Mock                              // ← Mock 객체
    private AccountRepository accountRepository;
    
    @Mock                              // ← Mock 객체
    private ResourceRequestRepository resourceRequestRepository;
    
    @InjectMocks
    private ResourceRequestService service;  // ← 실제 객체 (Mock 주입)
}
```

**특징:**
- ✅ `@ExtendWith(MockitoExtension.class)` → Spring 컨텍스트 없음
- ✅ `@Mock` → Repository는 Mock
- ✅ 실제 DB 사용 안 함
- ⚡ 실행 속도: 매우 빠름 (밀리초 단위)
- 🎯 목적: Service 로직만 검증

---

### 통합 테스트 (Integration Test) 예시

**파일:** `ResourceRequestStateBasedTest.java`

```java
@SpringBootTest              // ← 전체 컨텍스트 로드
@AutoConfigureMockMvc
@Transactional
class ResourceRequestStateBasedTest {
    
    @Autowired
    private AccountRepository accountRepository;  // ← 실제 Repository
    
    @Autowired
    private ResourceRepository resourceRepository;  // ← 실제 Repository
}
```

**특징:**
- ✅ `@SpringBootTest` → 전체 Spring 컨텍스트 로드
- ✅ `@Autowired` → 실제 Bean 주입
- ✅ 실제 DB 사용 (H2 또는 MySQL)
- ⚡ 실행 속도: 느림 (1-5초)
- 🎯 목적: 전체 시스템 통합 검증

---

## 프로젝트 내 테스트 분류

### 1. 단위 테스트 (Unit Test)

| 파일 | 어노테이션 | 특징 |
|------|----------|------|
| `ResourceRequestServiceTest` | `@ExtendWith(MockitoExtension.class)` | Service 로직만 테스트, Mock 사용 |

**예시:**
```java
@ExtendWith(MockitoExtension.class)  // Spring 없음!
class ResourceRequestServiceTest {
    @Mock
    private AccountRepository repository;  // Mock
}
```

---

### 2. Controller 테스트 (Web Layer Test)

| 파일 | 어노테이션 | 특징 |
|------|----------|------|
| `ResourceRequestControllerTest` | `@WebMvcTest` | Controller만 테스트, Service는 Mock |

**예시:**
```java
@WebMvcTest(ResourceRequestController.class)  // Controller만
class ResourceRequestControllerTest {
    @MockBean
    private ResourceRequestService service;  // Mock
}
```

---

### 3. Repository 테스트 (Integration Test - JPA)

| 파일 | 어노테이션 | 특징 |
|------|----------|------|
| `ResourceRequestRepositoryTest` | `@DataJpaTest` | JPA 레이어 테스트, H2 DB 사용 |

**예시:**
```java
@DataJpaTest  // JPA만
class ResourceRequestRepositoryTest {
    @Autowired
    private ResourceRequestRepository repository;  // 실제
}
```

---

### 4. 통합 테스트 (Integration Test - Full)

| 파일 | 어노테이션 | 특징 |
|------|----------|------|
| `ResourceRequestStateBasedTest` | `@SpringBootTest` | 전체 시스템 테스트, 실제 DB 사용 |

**예시:**
```java
@SpringBootTest  // 전체 컨텍스트
class ResourceRequestStateBasedTest {
    @Autowired
    private AccountRepository repository;  // 실제
}
```

---

## 테스트 피라미드

```
        /\
       /  \     E2E/통합 테스트 (Integration Test)
      /    \    - ResourceRequestStateBasedTest ✅
     /------\   - ResourceRequestIntegrationTest
    /        \  
   /          \  중간 레벨 테스트
  /------------\ - ResourceRequestRepositoryTest (@DataJpaTest)
 /              \ - ResourceRequestControllerTest (@WebMvcTest)
/----------------\ 단위 테스트 (Unit Test)
                 - ResourceRequestServiceTest (@ExtendWith) ✅
```

---

## 구분 기준

### 단위 테스트 (Unit Test)

✅ **다음이 있으면 단위 테스트:**
- `@ExtendWith(MockitoExtension.class)`
- `@Mock` 또는 `@MockBean` 사용
- Spring 컨텍스트 없음
- 실제 DB 사용 안 함

### 통합 테스트 (Integration Test)

✅ **다음이 있으면 통합 테스트:**
- `@SpringBootTest` 또는 `@DataJpaTest`
- `@Autowired`로 실제 Bean 주입
- 실제 Repository/DB 사용
- 전체 플로우 검증

---

## 요약

**ResourceRequestStateBasedTest는:**
- ❌ 단위 테스트가 아님
- ✅ **통합 테스트 (Integration Test)**
- ✅ 상태 기반 통합 테스트
- ✅ `@SpringBootTest` 사용
- ✅ 실제 DB 사용
- ⚡ 느림 (1-5초)
- 🎯 전체 시스템 검증

**단위 테스트 예시:**
- `ResourceRequestServiceTest` ✅
- `@ExtendWith(MockitoExtension.class)` 사용
- Mock 사용
- ⚡ 매우 빠름


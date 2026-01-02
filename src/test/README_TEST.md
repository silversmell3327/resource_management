# 테스트 코드 작성 가이드

이 프로젝트는 **Spring Boot**와 **JUnit 5 (JUnit Jupiter)**를 사용하여 테스트를 작성합니다.

## 테스트 종류별 설명

### 1. Controller 테스트 (`@WebMvcTest`)

**목적**: REST API 엔드포인트의 HTTP 요청/응답을 테스트

**특징**:
- 웹 레이어만 로드 (빠른 실행)
- `MockMvc`를 사용하여 HTTP 요청 시뮬레이션
- Service는 `@MockBean`으로 Mock 처리

**예시**: `controller/ResourceRequestControllerTest.java`

```java
@WebMvcTest(ResourceRequestController.class)
class ResourceRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ResourceRequestService service;
    
    @Test
    void test() throws Exception {
        mockMvc.perform(post("/api/endpoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }
}
```

### 2. Service 테스트 (`@ExtendWith(MockitoExtension.class)`)

**목적**: 비즈니스 로직을 단위 테스트

**특징**:
- Mockito를 사용하여 Repository를 Mock 처리
- 실제 DB 없이 빠르게 테스트
- 로직 검증에 집중

**예시**: `service/ResourceRequestServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class ResourceRequestServiceTest {
    @Mock
    private ResourceRequestRepository repository;
    
    @InjectMocks
    private ResourceRequestService service;
    
    @Test
    void test() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        // 테스트 실행
        service.someMethod();
        verify(repository, times(1)).save(any());
    }
}
```

### 3. Repository 테스트 (`@DataJpaTest`)

**목적**: JPA Repository와 Entity 매핑을 테스트

**특징**:
- 인메모리 H2 데이터베이스 사용 (실제 DB 불필요)
- `TestEntityManager`로 직접 데이터 조작 가능
- 실제 SQL 쿼리 검증 가능

**예시**: `repository/ResourceRequestRepositoryTest.java`

```java
@DataJpaTest
class ResourceRequestRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ResourceRequestRepository repository;
    
    @Test
    void test() {
        ResourceRequest entity = new ResourceRequest();
        ResourceRequest saved = repository.save(entity);
        Optional<ResourceRequest> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
    }
}
```

### 4. 통합 테스트 (`@SpringBootTest`)

**목적**: 전체 애플리케이션의 통합 동작을 테스트

**특징**:
- 전체 Spring 컨텍스트 로드
- 실제 DB 또는 Testcontainers 사용 가능
- `@Transactional`로 테스트 후 롤백

**예시**: `integration/ResourceRequestIntegrationTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResourceRequestIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ResourceRequestRepository repository;
    
    @Test
    void test() throws Exception {
        // 실제 DB에 저장하고 API 호출하여 전체 플로우 테스트
    }
}
```

## 테스트 실행 방법

### Gradle을 사용한 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "ResourceRequestControllerTest"

# 특정 패키지의 테스트만 실행
./gradlew test --tests "com.example.resourcemanagement.controller.*"

# 테스트 리포트 확인 (빌드 후)
./gradlew test
# 리포트 위치: build/reports/tests/test/index.html
```

### Eclipse에서 실행

1. 테스트 클래스에서 우클릭 → `Run As` → `JUnit Test`
2. 또는 프로젝트 우클릭 → `Run As` → `JUnit Test`

## 주요 어노테이션 설명

| 어노테이션 | 용도 |
|----------|------|
| `@Test` | 테스트 메서드임을 표시 |
| `@BeforeEach` | 각 테스트 전에 실행되는 설정 메서드 |
| `@AfterEach` | 각 테스트 후에 실행되는 정리 메서드 |
| `@DisplayName` | 테스트 이름을 한글로 표시 (JUnit 5) |
| `@Mock` | Mock 객체 생성 (Mockito) |
| `@MockBean` | Spring 컨텍스트에 Mock Bean 등록 |
| `@InjectMocks` | Mock 객체들을 주입받을 대상 |
| `@Transactional` | 테스트 후 롤백 (데이터 정리) |

## Assertion 메서드 (JUnit 5)

```java
// 기본 검증
assertEquals(expected, actual);
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);

// 예외 검증
assertThrows(IllegalArgumentException.class, () -> {
    service.method();
});

// 컬렉션 검증
assertThat(list).hasSize(3);
assertThat(list).contains(element);
```

## Mockito 주요 메서드

```java
// Mock 동작 설정
when(mock.method()).thenReturn(value);
when(mock.method()).thenThrow(exception);
when(mock.method()).thenAnswer(invocation -> { ... });

// Mock 호출 검증
verify(mock).method();
verify(mock, times(1)).method();
verify(mock, never()).method();
verify(mock, atLeastOnce()).method();
```

## 테스트 작성 가이드라인

1. **Given-When-Then 패턴 사용**
   - Given: 테스트 데이터 준비
   - When: 테스트 실행
   - Then: 결과 검증

2. **테스트는 독립적이어야 함**
   - 다른 테스트에 의존하지 않음
   - 실행 순서에 관계없이 동작해야 함

3. **명확한 테스트 이름**
   - `@DisplayName`으로 한글 이름 사용 권장
   - 메서드명은 `test[메서드명]_[시나리오]` 형식

4. **한 테스트에 한 가지 검증**
   - 여러 검증이 필요하면 여러 테스트로 분리

5. **Mock 사용 시 주의**
   - 필요한 것만 Mock 처리
   - 통합 테스트는 실제 객체 사용 권장



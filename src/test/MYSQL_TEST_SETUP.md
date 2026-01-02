# MySQL DB와 테스트 연동 설정

## 문제점

`@Transactional` 어노테이션이 있으면 테스트 후 자동 롤백되므로, 실제 DB의 데이터를 테스트에서 사용하기 어렵습니다.

## 해결 방법

### 방법 1: @Transactional 제거 (권장 - 실제 DB 사용)

```java
@SpringBootTest
@AutoConfigureMockMvc
// @Transactional  // ← 제거
@ActiveProfiles("test")
class ResourceRequestStateBasedTest {
    // ...
}
```

**장점:**
- 실제 MySQL DB 사용
- 테스트 후 데이터가 남아있음 (수동 정리 필요)

**단점:**
- 테스트 후 데이터 정리 필요
- 테스트 간 격리 안 됨

### 방법 2: @Rollback(false) 사용 (트랜잭션 유지, 롤백 안 함)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback(false)  // ← 추가: 롤백 안 함
@ActiveProfiles("test")
class ResourceRequestStateBasedTest {
    // ...
}
```

**장점:**
- 트랜잭션 관리 유지
- 실제 DB 사용
- 테스트 후 데이터 남음

**단점:**
- 테스트 후 데이터 정리 필요

### 방법 3: @Transactional 제거 + @Sql로 테스트 데이터 설정

```java
@SpringBootTest
@AutoConfigureMockMvc
// @Transactional 제거
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ResourceRequestStateBasedTest {
    // ...
}
```

## 현재 설정 확인

1. `application-test.properties` 파일 확인
   - MySQL 연결 정보 확인
   - `src/test/resources/application-test.properties`

2. 테스트 클래스 어노테이션 확인
   - `@ActiveProfiles("test")` 사용 중
   - `@Transactional` 사용 중 → 제거 또는 `@Rollback(false)` 추가

## 권장 설정

실제 MySQL DB를 사용하려면:

```java
@SpringBootTest
@AutoConfigureMockMvc
// @Transactional 제거 또는 @Rollback(false) 추가
@ActiveProfiles("test")
class ResourceRequestStateBasedTest {
    
    @BeforeEach
    void setUp() {
        // 테스트 전 초기화 (선택적)
        resourceRequestRepository.deleteAll();
        accountRepository.deleteAll();
        // resourceRepository.deleteAll();  // 실제 데이터는 유지
    }
    
    @AfterEach
    void tearDown() {
        // 테스트 후 정리 (필요시)
        resourceRequestRepository.deleteAll();
        accountRepository.deleteAll();
    }
}
```



# 상태 기반 테스트 작성 가이드

## 테스트 케이스 구조

상태 기반 테스트는 다음 구조를 따릅니다:

```
initialState → action → expectedState
```

### 1. 테스트 케이스 JSON 형식

```json
{
  "id": "TC1",
  "title": "테스트 케이스 제목",
  "initialState": {
    "accounts": [...],
    "projects": [...]
  },
  "action": {
    "type": "CreateResourceRequest",
    "payload": {
      "accountId": "A1",
      "requestedAt": "2025-12-31T09:00:00+09:00",
      "expiresAt": "2026-01-31T09:00:00+09:00",
      "message": "cpu 100",
      "resources": [...]
    }
  },
  "expectedState": {
    "accounts": [...],
    "projects": [...]
  }
}
```

## 테스트 작성 패턴

### 패턴 1: 직접 코드로 작성 (현재 구현)

```java
@Test
@DisplayName("TC1: 자원요청 누적으로 계정 quota 누적")
void testTC1_CreateResourceRequest_AccumulateQuota() throws Exception {
    // ========== Given: initialState 설정 ==========
    Account account = new Account();
    account.setName("Account-1");
    account.setAdmin("admin1");
    Account savedAccount = accountRepository.save(account);

    // ========== When: action 실행 ==========
    Map<String, Object> actionPayload = new HashMap<>();
    actionPayload.put("accountId", savedAccount.getId());
    actionPayload.put("message", "cpu 100");
    
    mockMvc.perform(post("/resource-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(actionPayload)))
            .andExpect(status().isCreated());

    // ========== Then: expectedState 검증 ==========
    Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());
    assertTrue(foundAccount.isPresent());
    // ... 추가 검증
}
```

### 패턴 2: JSON 파일에서 읽어서 실행 (향후 확장 가능)

```java
@Test
void testFromJsonFile() throws Exception {
    // JSON 파일 읽기
    TestCaseDto testCase = objectMapper.readValue(
        getClass().getResourceAsStream("/test-cases/resource-request-test-case.json"),
        TestCaseDto.class
    );

    // initialState 설정
    setupInitialState(testCase.getInitialState());
    
    // action 실행
    executeAction(testCase.getAction());
    
    // expectedState 검증
    verifyExpectedState(testCase.getExpectedState());
}
```

## 테스트 단계별 설명

### 1. initialState (초기 상태 설정)

```java
// Given: 초기 상태
Account account = new Account();
account.setName("Account-1");
account.setAdmin("admin1");
Account savedAccount = accountRepository.save(account);
```

**목적:**
- 테스트 전 초기 데이터 준비
- 테스트의 시작점 설정

### 2. action (액션 실행)

```java
// When: 액션 실행
Map<String, Object> payload = new HashMap<>();
payload.put("accountId", savedAccount.getId());
payload.put("message", "cpu 100");

mockMvc.perform(post("/resource-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isCreated());
```

**목적:**
- 실제 기능 실행
- API 호출 또는 메서드 실행

### 3. expectedState (예상 결과 검증)

```java
// Then: 예상 결과 검증
Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());
assertTrue(foundAccount.isPresent());
assertEquals("Account-1", foundAccount.get().getName());

List<ResourceRequest> requests = resourceRequestRepository.findAll();
assertEquals(1, requests.size());
```

**목적:**
- 결과 상태 검증
- 데이터 변경 확인
- 비즈니스 로직 검증

## 예시: TC1 테스트 케이스

```java
@Test
@DisplayName("TC1: 자원요청 누적으로 계정 quota 누적")
void testTC1() throws Exception {
    // Given: initialState
    // accounts: [{ id: "A1", name: "Account-1", resources: [] }]
    Account account = createAccount("A1", "Account-1", "admin1");
    
    // When: action
    // action: CreateResourceRequest with cpu 100
    createResourceRequest(account.getId(), "cpu 100", 100.0);
    
    // Then: expectedState
    // accounts: [{ id: "A1", resources: [{ type: "cpu", quota: 100.0 }] }]
    Account foundAccount = accountRepository.findById(account.getId()).get();
    assertNotNull(foundAccount);
    // resources 검증...
}
```

## 상태 기반 테스트의 장점

1. **명확한 구조**: Given-When-Then 패턴으로 이해하기 쉬움
2. **재사용성**: JSON 파일로 테스트 케이스를 관리 가능
3. **가독성**: 테스트 의도가 명확히 드러남
4. **확장성**: 새로운 테스트 케이스 추가가 쉬움

## 주의사항

1. **@Transactional 사용**: 테스트 후 자동 롤백
2. **@BeforeEach에서 초기화**: 각 테스트 전 깨끗한 상태
3. **독립성**: 각 테스트가 독립적으로 실행 가능해야 함
4. **명확한 검증**: expectedState를 명확히 검증

## 요약

상태 기반 테스트는:
- ✅ initialState로 시작 상태 설정
- ✅ action으로 기능 실행
- ✅ expectedState로 결과 검증

이 구조로 테스트를 작성하면 명확하고 유지보수하기 쉬운 테스트가 됩니다!


# Controller 테스트 작성 가이드

## Controller 테스트의 목적

Controller 테스트는 **REST API 엔드포인트**가 올바르게 동작하는지 검증합니다.

**테스트하는 것**:
- ✅ HTTP 요청이 올바르게 받아지는지
- ✅ HTTP 상태 코드가 올바른지 (200, 201, 400 등)
- ✅ JSON 요청/응답이 올바르게 처리되는지
- ✅ Service 메서드가 올바르게 호출되는지

**테스트하지 않는 것**:
- ❌ Service의 비즈니스 로직 (Service 테스트에서)
- ❌ DB 저장 (Repository 테스트에서)

---

## 기본 구조

```java
@WebMvcTest(ResourceRequestController.class)  // Controller만 로드
class ResourceRequestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;  // HTTP 요청 시뮬레이션
    
    @MockBean
    private ResourceRequestService service;  // Service는 Mock으로 대체
    
    @Test
    void test() throws Exception {
        // 1. 요청 데이터 준비
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("accountId", 1L);
        requestBody.put("message", "테스트");
        
        // 2. HTTP 요청 실행 및 검증
        mockMvc.perform(post("/resource-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated());  // 상태 코드 검증
        
        // 3. Service 메서드 호출 확인
        verify(service, times(1))
            .createResourceRequest(any(ResourceRequest.class));
    }
}
```

---

## 주요 어노테이션

### `@WebMvcTest(Controller.class)`
- 웹 레이어만 로드 (Controller, Filter, Interceptor 등)
- Service는 로드하지 않음 → `@MockBean`으로 대체
- 빠른 실행 속도

### `@MockBean`
- Spring 컨텍스트에 Mock Bean 등록
- Controller가 의존하는 Service를 Mock으로 대체

---

## MockMvc 사용법

### HTTP 메서드
```java
mockMvc.perform(get("/api/users/1"))      // GET
mockMvc.perform(post("/api/users"))       // POST
mockMvc.perform(put("/api/users/1"))      // PUT
mockMvc.perform(delete("/api/users/1"))   // DELETE
```

### 요청 설정
```java
mockMvc.perform(post("/resource-requests")
    .contentType(MediaType.APPLICATION_JSON)           // Content-Type 헤더
    .content("{\"accountId\":1,\"message\":\"test\"}") // 요청 본문
    .header("Authorization", "Bearer token")           // 커스텀 헤더
    .param("page", "1")                                // 쿼리 파라미터
)
```

### 응답 검증
```java
.andExpect(status().isOk())                    // 상태 코드 200
.andExpect(status().isCreated())               // 상태 코드 201
.andExpect(status().isBadRequest())            // 상태 코드 400
.andExpect(status().is4xxClientError())        // 4xx 에러
.andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Content-Type
.andExpect(jsonPath("$.id").value(1))          // JSON 필드 검증
.andExpect(jsonPath("$.name").value("test"))   // JSON 필드 검증
```

---

## 실제 예시

### 1. 성공 케이스

```java
@Test
@DisplayName("리소스 요청 생성 - 성공")
void testCreateResourceRequest_Success() throws Exception {
    // Given: 요청 데이터
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("accountId", 1L);
    requestBody.put("message", "테스트 메시지");

    // When & Then: HTTP 요청 실행 및 검증
    mockMvc.perform(post("/resource-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isCreated());  // 201 CREATED

    // Verify: Service 메서드 호출 확인
    verify(resourceRequestService, times(1))
        .createResourceRequest(any(ResourceRequest.class));
}
```

### 2. 실패 케이스 (잘못된 요청)

```java
@Test
@DisplayName("리소스 요청 생성 - accountId 누락")
void testCreateResourceRequest_MissingAccountId() throws Exception {
    // Given: accountId가 없는 잘못된 요청
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("message", "테스트 메시지");

    // When & Then: 400 Bad Request 예상
    mockMvc.perform(post("/resource-requests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isBadRequest());

    // Verify: Service는 호출되지 않아야 함
    verify(resourceRequestService, never())
        .createResourceRequest(any());
}
```

### 3. 응답 본문 검증

```java
@Test
@DisplayName("사용자 조회 - 성공")
void testGetUser_Success() throws Exception {
    // Given: Service Mock 설정
    UserDto userDto = new UserDto(1L, "홍길동");
    when(userService.getUser(1L)).thenReturn(userDto);

    // When & Then: HTTP 요청 및 응답 검증
    mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("홍길동"));
}
```

---

## verify() 사용법

### 기본 사용
```java
// 메서드가 1번 호출되었는지 확인
verify(service, times(1)).someMethod();

// 메서드가 호출되지 않았는지 확인
verify(service, never()).someMethod();

// 메서드가 최소 1번 호출되었는지 확인
verify(service, atLeastOnce()).someMethod();
```

### ArgumentMatcher 사용
```java
// any() - 어떤 값이든 허용
verify(service).createResourceRequest(any(ResourceRequest.class));

// eq() - 특정 값과 일치해야 함
verify(service).getUser(eq(1L));

// argThat() - 커스텀 조건
verify(service).createResourceRequest(
    argThat(request -> request.getMessage().equals("테스트"))
);
```

---

## 주의사항

### 1. 객체 비교 문제

❌ **나쁜 예**:
```java
ResourceRequest expected = new ResourceRequest();
expected.setAccount(account);
expected.setMessage("test");

verify(service).createResourceRequest(expected);  // 실패! 다른 객체
```

✅ **좋은 예**:
```java
verify(service).createResourceRequest(any(ResourceRequest.class));  // 어떤 객체든 OK
```

또는:

```java
verify(service).createResourceRequest(argThat(request -> 
    request.getMessage().equals("test") && 
    request.getAccount().getId().equals(1L)
));  // 조건으로 검증
```

### 2. Service가 void를 반환할 때

Service 메서드가 `void`를 반환하면 기본적으로 `doNothing()`이므로 별도 설정 불필요:

```java
// void 메서드는 기본적으로 아무것도 하지 않음
// doNothing().when(service).create(...);  // 필요 없음

mockMvc.perform(...)
    .andExpect(status().isCreated());

verify(service).create(any());  // 호출만 확인
```

### 3. Service가 값을 반환할 때

```java
// Given: Service Mock 설정
when(service.getUser(1L)).thenReturn(userDto);

// When & Then
mockMvc.perform(get("/users/1"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(1));
```

---

## 전체 테스트 흐름

```
1. Given (준비)
   - 요청 데이터 생성
   - Service Mock 설정 (필요시)

2. When (실행)
   - mockMvc.perform()으로 HTTP 요청

3. Then (검증)
   - andExpect()로 응답 검증
   - verify()로 Service 호출 확인
```

---

## 실전 팁

1. **테스트는 격리되어야 함**: 각 테스트는 독립적으로 실행 가능해야 함
2. **명확한 테스트 이름**: `@DisplayName`으로 한글로 명확하게 작성
3. **Given-When-Then 패턴**: 코드 가독성을 위해 일관된 패턴 사용
4. **필요한 것만 검증**: Controller 테스트에서는 HTTP 레이어만 검증
5. **Mock 사용**: Service는 Mock으로 처리하여 빠른 실행

---

## 요약

- ✅ `@WebMvcTest`로 Controller만 로드
- ✅ `@MockBean`으로 Service를 Mock 처리
- ✅ `MockMvc`로 HTTP 요청 시뮬레이션
- ✅ `andExpect()`로 응답 검증
- ✅ `verify()`로 Service 호출 확인
- ✅ 객체 비교 시 `any()` 또는 `argThat()` 사용



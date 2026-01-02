# JUnit 테스트 결과 형식

## Eclipse JUnit 뷰 결과

테스트를 실행하면 **JUnit 뷰**가 자동으로 열립니다.

### 성공한 경우 ✅

```
JUnit
└─ ResourceRequestStateBasedTest
   ├─ ✅ testTC1_CreateResourceRequest_AccumulateQuota (0.523s)
   └─ ✅ testStateBasedStructure (0.312s)

Runs: 2/2, Errors: 0, Failures: 0
```

**표시 내용:**
- ✅ 녹색 체크마크: 테스트 통과
- 테스트 메서드 이름
- 실행 시간 (예: 0.523초)
- 총 실행/에러/실패 개수

### 실패한 경우 ❌

```
JUnit
└─ ResourceRequestStateBasedTest
   ├─ ❌ testTC1_CreateResourceRequest_AccumulateQuota (0.125s)
   │   └─ java.lang.AssertionError: expected: <1> but was: <0>
   └─ ✅ testStateBasedStructure (0.312s)

Runs: 2/2, Errors: 0, Failures: 1
```

**실패한 테스트 클릭 시:**

```
Failure Trace:
org.opentest4j.AssertionFailedError: 
Expected :1
Actual   :0

at ResourceRequestStateBasedTest.testTC1_CreateResourceRequest_AccumulateQuota(ResourceRequestStateBasedTest.java:115)
```

## 콘솔 출력

테스트 실행 시 콘솔에도 로그가 출력됩니다:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v4.0.1)

2025-12-22 16:30:15.123  INFO ... : Starting ResourceRequestStateBasedTest
2025-12-22 16:30:15.456  INFO ... : Started ResourceRequestStateBasedTest in 0.333 seconds
```

## 테스트 상세 정보 확인

### 1. 테스트 트리 보기

JUnit 뷰에서 테스트를 펼치면:
```
ResourceRequestStateBasedTest
  ├─ testTC1_CreateResourceRequest_AccumulateQuota
  │   └─ (실행 시간)
  └─ testStateBasedStructure
      └─ (실행 시간)
```

### 2. 실패 원인 확인

실패한 테스트를 더블클릭하면:
- 해당 테스트 코드 위치로 이동
- Failure Trace 패널에 에러 메시지 표시

## 실제 실행 예시

### 성공 케이스

```
JUnit
├─ ResourceRequestStateBasedTest (0.835s)
│  ├─ ✅ testTC1_CreateResourceRequest_AccumulateQuota (0.523s)
│  └─ ✅ testStateBasedStructure (0.312s)

Runs: 2/2
Errors: 0
Failures: 0
```

### 실패 케이스

```
JUnit
├─ ResourceRequestStateBasedTest (0.437s)
│  ├─ ❌ testTC1_CreateResourceRequest_AccumulateQuota (0.125s)
│  │   └─ AssertionError: expected: <1> but was: <0>
│  └─ ✅ testStateBasedStructure (0.312s)

Runs: 2/2
Errors: 0
Failures: 1
```

**Failure Trace (클릭 시):**
```
org.opentest4j.AssertionFailedError: 
Expected :1
Actual   :0
    at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:65)
    at org.junit.jupiter.api.AssertEquals.assertEquals(AssertEquals.java:70)
    at com.example.resourcemanagement.integration.ResourceRequestStateBasedTest
        .testTC1_CreateResourceRequest_AccumulateQuota(ResourceRequestStateBasedTest.java:115)
```

## JUnit 뷰 위치

Eclipse 하단의 **JUnit** 탭에서 확인:
- Package Explorer 옆
- Console, Problems 등과 함께 있는 뷰
- 테스트 실행 시 자동으로 활성화됨

## 결과 해석

### Runs: 2/2
- 전체 2개 테스트 중 2개 실행됨

### Errors: 0
- 예외(exception) 발생한 테스트 수
- 컴파일 에러나 런타임 예외

### Failures: 1
- Assertion 실패한 테스트 수
- `assertEquals`, `assertTrue` 등 실패

## 디버깅 팁

1. **실패한 테스트 더블클릭**: 코드 위치로 이동
2. **Failure Trace 확인**: 어떤 assertion이 실패했는지 확인
3. **콘솔 로그 확인**: System.out.println() 출력 확인
4. **디버그 모드 실행**: Breakpoint 설정 후 Debug As → JUnit Test

## 요약

**성공 시:**
- ✅ 녹색 체크마크
- Runs: X/X, Errors: 0, Failures: 0
- 각 테스트별 실행 시간 표시

**실패 시:**
- ❌ 빨간 X
- Runs: X/X, Errors: Y, Failures: Z
- 실패한 테스트 클릭 시 상세 에러 정보 확인 가능


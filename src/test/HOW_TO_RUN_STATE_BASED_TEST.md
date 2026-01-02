# 상태 기반 테스트 실행 방법 (Eclipse)

## 간단한 실행 방법

### 1단계: 테스트 파일 열기
```
src/test/java/com/example/resourcemanagement/integration/
  → ResourceRequestStateBasedTest.java
```

### 2단계: 테스트 실행

**옵션 1: 전체 테스트 클래스 실행**
1. 파일에서 **우클릭**
2. **Run As** → **JUnit Test**
3. 클래스 내 모든 테스트 메서드 실행

**옵션 2: 특정 테스트 메서드만 실행**
1. 실행하고 싶은 메서드 이름 왼쪽의 **▶️ 버튼** 클릭
2. 또는 메서드에서 **우클릭** → **Run As** → **JUnit Test**

**옵션 3: 단축키 사용**
- `Alt + Shift + X, T` → JUnit 테스트 실행

## 실행 예시

### 전체 테스트 실행
```
ResourceRequestStateBasedTest
  ├─ testTC1_CreateResourceRequest_AccumulateQuota ✅
  └─ testStateBasedStructure ✅
```

### 특정 테스트만 실행
```
testTC1_CreateResourceRequest_AccumulateQuota ✅
```

## JUnit 뷰에서 결과 확인

테스트 실행 후 JUnit 뷰가 자동으로 열립니다:

```
JUnit [ResourceRequestStateBasedTest]
  ├─ ✅ testTC1_CreateResourceRequest_AccumulateQuota (0.5s)
  └─ ✅ testStateBasedStructure (0.3s)

Runs: 2/2, Errors: 0, Failures: 0
```

- ✅ **녹색 체크**: 테스트 통과
- ❌ **빨간 X**: 테스트 실패 (클릭하면 상세 정보 확인)
- ⏱️ **실행 시간**: 각 테스트 소요 시간

## 실패한 테스트 확인

테스트가 실패하면:
1. **빨간 X** 표시가 나타남
2. 실패한 테스트 클릭
3. 아래 패널에서 **Failure Trace** 확인
4. 에러 메시지와 스택 트레이스 확인

## 빠른 실행 팁

1. **마지막 실행 재실행**: `Ctrl + F11`
2. **테스트 메서드 이름 옆 ▶️ 버튼**: 클릭 한 번으로 실행
3. **패키지 전체 실행**: 패키지 폴더 우클릭 → Run As → JUnit Test

## 문제 해결

### 테스트가 실행되지 않을 때
- 프로젝트 우클릭 → **Refresh** (F5)
- 프로젝트 우클릭 → **Build Project**
- Gradle 프로젝트인 경우: 우클릭 → **Gradle** → **Refresh Gradle Project**

### JUnit 뷰가 안 보일 때
- **Window** → **Show View** → **Other** → **JUnit** → **JUnit**

## 요약

가장 쉬운 방법:
1. `ResourceRequestStateBasedTest.java` 파일 열기
2. 우클릭 → **Run As** → **JUnit Test**
3. 끝! ✅


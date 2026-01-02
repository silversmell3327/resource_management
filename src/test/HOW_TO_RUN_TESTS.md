# 테스트 실행 방법

## 1. Eclipse에서 실행

### 방법 1: 개별 테스트 클래스 실행
1. 테스트 클래스 파일 열기 (예: `ResourceRequestControllerTest.java`)
2. 우클릭 → **Run As** → **JUnit Test**
3. 또는 `Alt + Shift + X`, `T` (단축키)

### 방법 2: 특정 테스트 메서드만 실행
1. 실행하고 싶은 테스트 메서드에 커서 놓기
2. 우클릭 → **Run As** → **JUnit Test**
3. 또는 메서드 이름 왼쪽의 ▶️ 버튼 클릭

### 방법 3: 프로젝트 전체 테스트 실행
1. 프로젝트 우클릭 → **Run As** → **JUnit Test**
2. 또는 `src/test` 폴더 우클릭 → **Run As** → **JUnit Test**

### 방법 4: 패키지 단위 실행
1. 패키지 폴더 우클릭 (예: `controller` 폴더)
2. **Run As** → **JUnit Test**

---

## 2. Gradle로 실행 (터미널/명령 프롬프트)

### 현재 디렉토리 확인
```bash
# Windows PowerShell
pwd

# 현재 위치가 프로젝트 루트인지 확인
# C:\Users\ssm06\eclipse-workspace\resource-management
```

### 모든 테스트 실행
```bash
# Windows
./gradlew test

# 또는
gradlew.bat test
```

### 특정 테스트 클래스만 실행
```bash
./gradlew test --tests "ResourceRequestControllerTest"

# 패키지 전체
./gradlew test --tests "com.example.resourcemanagement.controller.*"

# 특정 패턴
./gradlew test --tests "*ControllerTest"
```

### 특정 테스트 메서드만 실행
```bash
./gradlew test --tests "ResourceRequestControllerTest.testCreateResourceRequest_Success"
```

### 테스트 결과 확인
```bash
# 테스트 실행 후 리포트 위치
build/reports/tests/test/index.html

# 브라우저로 열기 (Windows)
start build/reports/tests/test/index.html
```

---

## 3. Eclipse에서 Gradle 테스트 실행

### 방법 1: Gradle Tasks 실행
1. 프로젝트 우클릭 → **Run As** → **Gradle Build...**
2. **Gradle Tasks**에서 `test` 선택
3. **Run** 클릭

### 방법 2: Gradle View 사용
1. **Window** → **Show View** → **Other** → **Gradle** → **Gradle Tasks**
2. 프로젝트 → **resource-management** → **verification** → **test** 더블클릭

---

## 4. 실행 결과 확인

### Eclipse JUnit View
- 테스트 실행 후 자동으로 JUnit 뷰가 열림
- ✅ 녹색: 테스트 통과
- ❌ 빨간색: 테스트 실패
- 실패한 테스트 클릭하면 실패 원인 확인 가능

### 콘솔 출력
```
> Task :test
ResourceRequestControllerTest > 리소스 요청 생성 - 성공 PASSED
ResourceRequestServiceTest > 리소스 요청 생성 - 성공 PASSED
...

BUILD SUCCESSFUL in 3s
```

---

## 5. 빠른 실행 팁

### Eclipse 단축키
- `Ctrl + F11`: 마지막 실행 (이전에 테스트 실행했다면)
- `Alt + Shift + X, T`: JUnit 테스트 실행
- `Ctrl + Shift + F10` (IntelliJ 단축키, Eclipse는 다를 수 있음)

### 테스트 실행 범위 선택
```
클래스 레벨:    @Test 메서드 위에 커서 → 우클릭 → Run As → JUnit Test
메서드 레벨:    메서드 이름 왼쪽 ▶️ 버튼 클릭
패키지 레벨:    패키지 폴더 우클릭 → Run As → JUnit Test
프로젝트 레벨:  프로젝트 우클릭 → Run As → JUnit Test
```

---

## 6. 테스트 실행 전 확인사항

### 1. 프로젝트 빌드 확인
```bash
# Gradle로 빌드 (테스트 포함)
./gradlew build

# 테스트만 실행
./gradlew test
```

### 2. Eclipse 프로젝트 새로고침
- 프로젝트 우클릭 → **Refresh** (F5)
- Gradle 프로젝트인 경우: 우클릭 → **Gradle** → **Refresh Gradle Project**

### 3. 컴파일 오류 확인
- Eclipse Problems 뷰 확인
- 빨간 X 표시가 없어야 함

---

## 7. 문제 해결

### 테스트가 실행되지 않을 때
1. **프로젝트 새로고침**: F5
2. **Gradle 프로젝트 새로고침**: 우클릭 → Gradle → Refresh Gradle Project
3. **Clean Build**: 프로젝트 우클릭 → Clean... → Build

### JUnit이 인식되지 않을 때
1. 프로젝트 우클릭 → **Properties** → **Java Build Path** → **Libraries**
2. JUnit 라이브러리가 있는지 확인
3. 없다면 Gradle 프로젝트 새로고침

### 테스트가 느릴 때
- 특정 테스트 클래스만 실행
- 불필요한 통합 테스트는 제외하고 단위 테스트만 실행

---

## 8. 실제 실행 예시

### Eclipse에서
```
1. ResourceRequestControllerTest.java 파일 열기
2. 우클릭 → Run As → JUnit Test
3. JUnit 뷰에서 결과 확인:
   ✅ ResourceRequestControllerTest > 리소스 요청 생성 - 성공
   ✅ ResourceRequestControllerTest > 리소스 요청 생성 - 잘못된 요청
   Runs: 2/2, Errors: 0, Failures: 0
```

### 터미널에서
```bash
PS C:\Users\ssm06\eclipse-workspace\resource-management> ./gradlew test

> Task :test
ResourceRequestControllerTest > 리소스 요청 생성 - 성공 PASSED
ResourceRequestControllerTest > 리소스 요청 생성 - 잘못된 요청 PASSED
ResourceRequestServiceTest > 리소스 요청 생성 - 성공 PASSED
...

BUILD SUCCESSFUL in 4.5s
```

---

## 요약

**가장 쉬운 방법 (Eclipse)**:
1. 테스트 클래스 파일 열기
2. 우클릭 → **Run As** → **JUnit Test**
3. 끝! ✅

**터미널에서 (빠름)**:
```bash
./gradlew test
```



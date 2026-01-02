# MySQL DB 연동 문제 해결 가이드

## 확인 사항

### 1. MySQL 서버 실행 확인
```bash
# MySQL이 실행 중인지 확인
mysql -u root -p
```

### 2. application-test.properties 파일 위치 확인
- 파일 위치: `src/test/resources/application-test.properties`
- 파일이 있어야 `@ActiveProfiles("test")`가 작동함

### 3. 테스트 실행 시 확인할 것들

#### 에러 메시지 확인
- **Connection refused**: MySQL 서버가 실행되지 않음
- **Access denied**: 사용자 이름/비밀번호 오류
- **Unknown database**: `resource_management` DB가 없음
- **ClassNotFoundException**: MySQL 드라이버가 없음

#### 로그 확인
- 테스트 실행 시 콘솔에 SQL 쿼리가 출력되는지 확인
- `spring.jpa.show-sql=true`가 설정되어 있으면 SQL이 보여야 함

### 4. 일반적인 문제 해결

#### 문제 1: MySQL DB가 없음
```sql
CREATE DATABASE resource_management;
```

#### 문제 2: application-test.properties 파일이 로드되지 않음
- 파일 이름 확인: `application-test.properties` (정확히)
- 파일 위치 확인: `src/test/resources/`
- Eclipse에서 프로젝트 Refresh (F5)

#### 문제 3: MySQL 드라이버가 없음
`build.gradle`에 다음이 있는지 확인:
```gradle
runtimeOnly 'com.mysql:mysql-connector-j'
```

## 디버깅 코드 추가

테스트에 다음 로그를 추가하여 연결 상태 확인:

```java
@BeforeEach
void setUp() {
    System.out.println("=== DB 연결 확인 ===");
    System.out.println("DataSource: " + dataSource);
    // Resource 조회 테스트
    long count = resourceRepository.count();
    System.out.println("Resource 개수: " + count);
}
```



# ResourceType Enum 불일치 문제 해결

## 문제

DB에는 `type`이 소문자로 저장되어 있습니다:
```sql
| type |
| cpu  |  -- 소문자
```

하지만 Java의 `ResourceType` enum은 대문자로 정의되어 있습니다:
```java
public enum ResourceType {
    CPU,    // 대문자
    MEMORY,
    GPU,
    STORAGE
}
```

Hibernate가 DB에서 "cpu"를 읽어서 `ResourceType.cpu`를 찾으려고 하지만, enum에는 `CPU`만 있어서 에러가 발생합니다.

## 해결 방법

### 방법 1: DB의 값을 대문자로 변경 (권장)

```sql
UPDATE resource SET type = 'CPU' WHERE type = 'cpu';
```

### 방법 2: ResourceType enum에 @JsonValue 어노테이션 추가 (JSON 직렬화용)

하지만 이건 Hibernate와는 관련이 없습니다.

### 방법 3: EnumType을 ORDINAL로 변경 (비권장)

### 방법 4: DB 값과 enum 이름을 일치시키기

DB의 값을 enum과 일치시켜야 합니다.

## 권장 해결책

DB의 값을 대문자로 업데이트:
```sql
UPDATE resource SET type = 'CPU' WHERE type = 'cpu';
```

또는 enum의 이름을 소문자로 바꿀 수도 있지만, Java 컨벤션상 enum은 대문자로 작성하는 것이 일반적입니다.


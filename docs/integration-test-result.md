# 통합 테스트 결과 보고서

- **테스트 일시**: 2026-04-13
- **환경**: PostgreSQL 16 + MongoDB 7 (Docker Compose) / Spring Boot 3.4.0 / Java 17
- **프로파일**: dev (로컬)

---

## 테스트 환경 구성

```bash
# DB 볼륨 초기화 후 깨끗한 상태에서 시작
docker compose down -v
docker compose up -d postgres mongodb

# 앱 실행
./gradlew bootRun
```

- PostgreSQL: `localhost:5432/monew` (user: monew / pw: monew)
- MongoDB: `mongodb://monew:monew@localhost:27017/monew?authSource=admin`
- 기사 데이터는 테스트용으로 PostgreSQL에 직접 INSERT

---

## 1. User 도메인

### 1-1. 회원가입

```
POST /api/users
Content-Type: application/json

{"email":"test@example.com","nickname":"테스트유저","password":"test12345"}
```

**응답** (200):
```json
{
  "id": "d4cae8fa-ee25-430d-b6fb-60cee32c1e51",
  "email": "test@example.com",
  "nickname": "테스트유저",
  "createdAt": "2026-04-13T06:45:39.605981Z"
}
```

**결과**: ✅ 정상 — UUID PK 생성, createdAt 자동 설정

### 1-2. 회원가입 (두 번째 사용자)

```
POST /api/users

{"email":"test2@example.com","nickname":"유저2","password":"pass12345"}
```

**응답** (200): 정상 생성  
**결과**: ✅

### 1-3. 로그인

```
POST /api/users/login

{"email":"test@example.com","password":"test12345"}
```

**응답** (200): 사용자 정보 반환 (BCrypt 매칭 정상)  
**결과**: ✅

### 1-4. 중복 이메일 가입 시도

```
POST /api/users

{"email":"test@example.com","nickname":"다른닉네임","password":"pass12345"}
```

**응답** (409):
```json
{
  "code": "DUPLICATE_USER",
  "message": "이미 존재하는 사용자입니다",
  "details": {"email": "test@example.com"}
}
```

**결과**: ✅ 중복 방지 정상

---

## 2. Interest 도메인

### 2-1. 관심사 생성

```
POST /api/interests
MoNew-Request-User-ID: {userId}

{"name":"클로드","keywords":["Claude","Anthropic","AI assistant"]}
```

**응답** (200):
```json
{
  "id": "ffa01d7d-ab7b-4113-be2a-b773dc2dcc61",
  "name": "클로드",
  "keywords": ["Claude", "Anthropic", "AI assistant"],
  "subscriberCount": 0,
  "subscribedByMe": false
}
```

**결과**: ✅ 키워드 포함 정상 생성

### 2-2. 관심사 생성 (인공지능)

```
POST /api/interests

{"name":"인공지능","keywords":["AI","GPT","LLM"]}
```

**결과**: ✅

### 2-3. 관심사 구독

```
POST /api/interests/{interestId}/subscriptions
MoNew-Request-User-ID: {userId}
```

**응답** (200): subscriberCount=1, subscribedByMe=true  
**결과**: ✅ 구독 카운트 증가 및 구독 여부 반영

### 2-4. 관심사 목록 조회 (이름순)

```
GET /api/interests?orderBy=name
MoNew-Request-User-ID: {userId}
```

**응답** (200):
```json
{
  "content": [
    {"name": "스프링", "subscriberCount": 0, "subscribedByMe": false},
    {"name": "인공지능", "subscriberCount": 1, "subscribedByMe": false},
    {"name": "클로드", "subscriberCount": 1, "subscribedByMe": true}
  ],
  "hasNext": false
}
```

**결과**: ✅ 이름 가나다순 정렬, subscribedByMe 사용자별 반영

### 2-5. 관심사 목록 조회 (구독자순)

```
GET /api/interests?orderBy=subscriberCount
```

**결과**: ✅ 구독자 수 기준 정렬 정상

---

## 3. Article 도메인

> 기사 데이터는 테스트용으로 직접 INSERT (뉴스 수집 스케줄러 비활성 상태)

### 3-1. 기사 목록 조회

```
GET /api/articles
MoNew-Request-User-ID: {userId}
```

**응답** (200): 3건 반환, 커서 페이징 구조 정상
```json
{
  "content": [
    {"title": "Claude AI 최신 업데이트", "viewCount": 0, "viewedByMe": false},
    {"title": "GPT-5 출시 소식", "viewCount": 0, "viewedByMe": false},
    {"title": "LLM 트렌드 분석", "viewCount": 0, "viewedByMe": false}
  ],
  "hasNext": false
}
```

**결과**: ✅

### 3-2. 기사 조회 (첫 조회 — viewCount 증가)

```
POST /api/articles/{articleId}/views
MoNew-Request-User-ID: {userId}
```

**응답** (200): viewCount=1, viewedByMe=true  
**결과**: ✅ ArticleView 생성 및 카운트 증가

### 3-3. 기사 재조회 (중복 방지)

같은 사용자가 동일 기사 재조회 시:

**응답** (200): viewCount=1 유지 (증가하지 않음)  
**결과**: ✅ unique 조회 기록으로 중복 방지

### 3-4. 기사 논리 삭제

```
DELETE /api/articles/{articleId}
```

**응답**: 204 No Content  
**결과**: ✅

### 3-5. 삭제된 기사 조회 시도

```
POST /api/articles/{삭제된 articleId}/views
```

**응답** (404):
```json
{
  "code": "ARTICLE_NOT_FOUND",
  "message": "뉴스 기사를 찾을 수 없습니다",
  "details": {"articleId": "aaaaaaaa-0001-0001-0001-000000000003"}
}
```

**결과**: ✅ 논리 삭제된 기사 접근 차단

---

## 4. Comment 도메인

### 4-1. 댓글 작성

```
POST /api/comments
MoNew-Request-User-ID: {userId}

{"articleId":"{articleId}","content":"Claude가 정말 좋아졌네요!"}
```

**응답** (200):
```json
{
  "id": "85b60934-...",
  "userNickname": "테스트유저",
  "content": "Claude가 정말 좋아졌네요!",
  "likeCount": 0,
  "likedByMe": false
}
```

**결과**: ✅ Article의 commentCount도 증가

### 4-2. 댓글 목록 조회

```
GET /api/comments?articleId={articleId}
MoNew-Request-User-ID: {userId}
```

**응답** (200): 2건, 최신순 정렬  
**결과**: ✅

### 4-3. 댓글 좋아요

```
POST /api/comments/{commentId}/likes
MoNew-Request-User-ID: {다른 userId}
```

**응답** (200): likeCount=1, likedByMe=true  
**결과**: ✅ 좋아요 시 알림도 자동 생성됨

### 4-4. 좋아요 취소

```
DELETE /api/comments/{commentId}/likes
MoNew-Request-User-ID: {userId}
```

**응답** (200): likeCount=0, likedByMe=false  
**결과**: ✅

### 4-5. 댓글 수정

```
PATCH /api/comments/{commentId}
MoNew-Request-User-ID: {userId}

{"content":"Claude Opus 4가 정말 좋아졌네요! (수정됨)"}
```

**응답** (200): content 변경 반영, updatedAt 갱신  
**결과**: ✅

### 4-6. 댓글 삭제

```
DELETE /api/comments/{commentId}
MoNew-Request-User-ID: {userId}
```

**응답**: 204 No Content  
**결과**: ✅ 논리 삭제

### 4-7. 빈 댓글 작성 (유효성 검증)

```
POST /api/comments

{"articleId":"...","content":""}
```

**응답** (400):
```json
{
  "code": "VALIDATION_FAILED",
  "message": "입력값 검증에 실패했습니다",
  "details": {"content": "공백일 수 없습니다"}
}
```

**결과**: ✅ @Valid 유효성 검증 정상

---

## 5. Notification 도메인

### 5-1. 알림 자동 생성

댓글 좋아요 시 댓글 작성자에게 알림이 자동 생성됨:

```json
{
  "content": "유저2님이 회원님의 댓글을 좋아합니다.",
  "resourceType": "COMMENT",
  "resourceId": "85b60934-...",
  "confirmed": false
}
```

**결과**: ✅ 이벤트 기반 알림 자동 생성

### 5-2. 알림 목록 조회

```
GET /api/notifications
MoNew-Request-User-ID: {userId}
```

**응답** (200): 미확인 알림 1건  
**결과**: ✅

### 5-3. 알림 전체 확인

```
PATCH /api/notifications
MoNew-Request-User-ID: {userId}
```

**응답** (200): `{"updated": 1}`  
**결과**: ✅

---

## 6. Activity 도메인 (MongoDB)

### 6-1. 활동 내역 조회

```
GET /api/user-activities/{userId}
```

**응답** (200):
```json
{
  "userId": "d4cae8fa-...",
  "nickname": "테스트유저",
  "subscriptions": [
    {"interestName": "클로드", "interestKeywords": ["Claude", "AI assistant", "Anthropic"]}
  ],
  "recentComments": [
    {"articleTitle": "Claude AI 최신 업데이트", "content": "Claude가 정말 좋아졌네요!"}
  ],
  "recentCommentLikes": [],
  "recentViewedArticles": [
    {"articleTitle": "Claude AI 최신 업데이트", "source": "TEST", "articleViewCount": 1}
  ]
}
```

**결과**: ✅ PostgreSQL 활동(구독/댓글/조회) 시 MongoDB document 자동 동기화 정상

---

## 발견 및 수정한 버그

### 버그 1: InterestRepository — distinct + left join fetch + Pageable

- **증상**: `GET /api/interests` 호출 시 500 에러 (`InvalidDataAccessResourceUsageException`)
- **원인**: PostgreSQL에서 `select distinct` + `left join fetch` + `Pageable(limit)` 조합 시, `distinct` 쿼리의 SELECT 목록과 ORDER BY 컬럼 불일치로 SQL 실행 실패
- **수정**: `distinct` + `left join fetch` 제거, `@EntityGraph(attributePaths = "keywords")`로 변경
- **파일**: `src/main/java/com/monew/domain/interest/repository/InterestRepository.java`

### 버그 2: InterestRepository — lower(bytea) 타입 에러

- **증상**: 버그 1 수정 후에도 동일하게 500 에러 (`PSQLException: function lower(bytea) does not exist`)
- **원인**: JPQL `lower(concat('%', :keyword, '%'))`에서 Hibernate가 `:keyword` 파라미터를 `bytea` 타입으로 바인딩. PostgreSQL에는 `lower(bytea)` 함수가 없음
- **수정**: `concat('%', :keyword, '%')` → `concat('%', cast(:keyword as string), '%')` 명시적 캐스팅 추가
- **파일**: `src/main/java/com/monew/domain/interest/repository/InterestRepository.java`

### 추가 수정: ArticleServiceTest 컴파일 경고

- **증상**: `any(Specification.class)` 사용 시 raw type unchecked 경고
- **수정**: `ArgumentMatchers.<Specification<Article>>any()`로 변경
- **파일**: `src/test/java/com/monew/domain/article/service/ArticleServiceTest.java`

---

## 전체 결과 요약

| 도메인 | 테스트 항목 수 | 성공 | 실패 |
|--------|:---:|:---:|:---:|
| User | 4 | 4 | 0 |
| Interest | 5 | 5 | 0 (버그 2건 수정 후) |
| Article | 5 | 5 | 0 |
| Comment | 7 | 7 | 0 |
| Notification | 3 | 3 | 0 |
| Activity | 1 | 1 | 0 |
| **합계** | **25** | **25** | **0** |

단위 테스트(`./gradlew clean test`): ✅ 전체 통과, 컴파일 경고 0건, JaCoCo 커버리지 검증 통과

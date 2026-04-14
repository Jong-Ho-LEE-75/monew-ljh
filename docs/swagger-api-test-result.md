# Swagger API 전체 엔드포인트 테스트 결과

- **테스트 일시**: 2026-04-13
- **환경**: PostgreSQL 16 + MongoDB 7 (Docker Compose) / Spring Boot 3.4.0 / Java 17
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI 스펙**: http://localhost:8080/v3/api-docs

---

## 엔드포인트 목록 (총 26개)

| # | 메서드 | 경로 | 컨트롤러 | operationId |
|---|--------|------|----------|-------------|
| 1 | POST | `/api/users` | user-controller | register |
| 2 | POST | `/api/users/login` | user-controller | login |
| 3 | GET | `/api/users/{userId}` | user-controller | find |
| 4 | PATCH | `/api/users/{userId}` | user-controller | update |
| 5 | DELETE | `/api/users/{userId}` | user-controller | softDelete |
| 6 | POST | `/api/interests` | interest-controller | register |
| 7 | GET | `/api/interests` | interest-controller | findAll |
| 8 | PATCH | `/api/interests/{interestId}` | interest-controller | updateKeywords |
| 9 | DELETE | `/api/interests/{interestId}` | interest-controller | delete |
| 10 | POST | `/api/interests/{interestId}/subscriptions` | interest-controller | subscribe |
| 11 | DELETE | `/api/interests/{interestId}/subscriptions` | interest-controller | unsubscribe |
| 12 | GET | `/api/articles` | article-controller | findAll |
| 13 | POST | `/api/articles/{articleId}/views` | article-controller | view |
| 14 | DELETE | `/api/articles/{articleId}` | article-controller | softDelete |
| 15 | POST | `/api/comments` | comment-controller | create |
| 16 | GET | `/api/comments` | comment-controller | findByArticle |
| 17 | PATCH | `/api/comments/{commentId}` | comment-controller | update |
| 18 | DELETE | `/api/comments/{commentId}` | comment-controller | softDelete |
| 19 | POST | `/api/comments/{commentId}/likes` | comment-controller | like |
| 20 | DELETE | `/api/comments/{commentId}/likes` | comment-controller | unlike |
| 21 | GET | `/api/notifications` | notification-controller | findUnconfirmed |
| 22 | PATCH | `/api/notifications/{notificationId}` | notification-controller | confirm |
| 23 | PATCH | `/api/notifications` | notification-controller | confirmAll |
| 24 | GET | `/api/user-activities/{userId}` | user-activity-controller | getActivity |
| 25 | GET | `/api/articles/backups` | article-backup-controller | listBackupDates |
| 26 | POST | `/api/articles/backups/restore` | article-backup-controller | restore |

---

## 테스트 결과 상세

### user-controller (5개 엔드포인트)

#### 1. POST /api/users (회원가입)

```
Request:
  POST /api/users
  Content-Type: application/json
  {"email":"swagger@test.com","nickname":"스웨거유저","password":"swagger123"}

Response: 201 Created
  {"id":"0589c566-...","email":"swagger@test.com","nickname":"스웨거유저","createdAt":"2026-04-13T07:32:26.788096Z"}
```

**결과**: ✅ 정상 — UUID 자동 생성, BCrypt 암호화 저장

#### 2. POST /api/users/login (로그인)

```
Request:
  POST /api/users/login
  {"email":"swagger@test.com","password":"swagger123"}

Response: 200 OK
  {"id":"0589c566-...","email":"swagger@test.com","nickname":"스웨거유저"}
```

**결과**: ✅ 정상 — BCrypt 매칭 성공

#### 3. GET /api/users/{userId} (사용자 조회)

```
Request:
  GET /api/users/d4cae8fa-ee25-430d-b6fb-60cee32c1e51

Response: 200 OK
  {"id":"d4cae8fa-...","email":"test@example.com","nickname":"테스트유저"}
```

**결과**: ✅

#### 4. PATCH /api/users/{userId} (사용자 수정)

```
Request:
  PATCH /api/users/d4cae8fa-...
  MoNew-Request-User-ID: d4cae8fa-...
  {"nickname":"수정된유저"}

Response: 200 OK
  {"nickname":"수정된유저"}
```

**결과**: ✅ 닉네임 변경 반영

#### 5. DELETE /api/users/{userId} (사용자 논리 삭제)

```
Request:
  DELETE /api/users/0589c566-...
  MoNew-Request-User-ID: 0589c566-...

Response: 204 No Content
```

삭제 후 조회 시:
```
GET /api/users/0589c566-...
→ 404 {"code":"USER_NOT_FOUND","message":"사용자를 찾을 수 없습니다"}
```

**결과**: ✅ 논리 삭제 후 조회 차단

---

### interest-controller (6개 엔드포인트)

#### 6. POST /api/interests (관심사 생성)

```
Request:
  POST /api/interests
  MoNew-Request-User-ID: {userId}
  {"name":"프로그래밍","keywords":["Java","Python","코딩"]}

Response: 201 Created
  {"id":"5c732258-...","name":"프로그래밍","keywords":["Java","Python","코딩"],"subscriberCount":0,"subscribedByMe":false}
```

**결과**: ✅

#### 7. GET /api/interests (관심사 목록)

```
Request:
  GET /api/interests?orderBy=name&size=10
  MoNew-Request-User-ID: {userId}

Response: 200 OK
  {"content":[
    {"name":"스프링","subscriberCount":0,"subscribedByMe":false},
    {"name":"인공지능","subscriberCount":1,"subscribedByMe":false},
    {"name":"클로드","subscriberCount":1,"subscribedByMe":true},
    {"name":"프로그래밍","subscriberCount":0,"subscribedByMe":false}
  ],"hasNext":false}
```

**결과**: ✅ 이름 가나다순 정렬, subscribedByMe 사용자별 반영

#### 7-1. GET /api/interests?keyword=AI (키워드 검색)

```
Response: 200 OK
  2건 반환 — "인공지능"(키워드 AI 포함), "클로드"(키워드 AI assistant 포함)
```

**결과**: ✅ 관심사명 + 키워드 모두에서 LIKE 검색 동작

#### 8. PATCH /api/interests/{interestId} (키워드 수정)

```
Request:
  PATCH /api/interests/1ce5a5fc-...
  {"keywords":["Spring Boot","Spring Security","JPA","Hibernate"]}

Response: 200 OK
  {"name":"스프링","keywords":["Spring Boot","Spring Security","JPA","Hibernate"]}
```

**결과**: ✅ 키워드 교체 (orphanRemoval) 정상

#### 9. DELETE /api/interests/{interestId} (관심사 삭제)

```
Request:
  DELETE /api/interests/5c732258-...

Response: 204 No Content
```

**결과**: ✅

#### 10. POST /api/interests/{interestId}/subscriptions (구독)

```
Request:
  POST /api/interests/1ce5a5fc-.../subscriptions
  MoNew-Request-User-ID: {userId}

Response: 201 Created
  {"name":"스프링","subscriberCount":1,"subscribedByMe":true}
```

**결과**: ✅ subscriberCount 증가

#### 11. DELETE /api/interests/{interestId}/subscriptions (구독 취소)

```
Request:
  DELETE /api/interests/1ce5a5fc-.../subscriptions

Response: 204 No Content
```

**결과**: ✅ subscriberCount 감소

---

### article-controller (3개 엔드포인트)

#### 12. GET /api/articles (기사 목록)

```
Request:
  GET /api/articles
  MoNew-Request-User-ID: {userId}

Response: 200 OK
  2건 반환 (논리 삭제된 기사는 제외)
  - "Claude AI 최신 업데이트" (viewCount:1, commentCount:1, viewedByMe:true)
  - "GPT-5 출시 소식" (viewCount:0, viewedByMe:false)
```

**결과**: ✅

#### 12-1. GET /api/articles?keyword=Claude (키워드 검색)

```
Response: 200 OK — 1건 ("Claude AI 최신 업데이트")
```

**결과**: ✅ 제목/요약 LIKE 검색

#### 12-2. GET /api/articles?sortBy=VIEW_COUNT&direction=DESC (정렬)

```
Response: 200 OK — viewCount 내림차순 정렬
```

**결과**: ✅

#### 13. POST /api/articles/{articleId}/views (기사 조회)

```
Request:
  POST /api/articles/aaaaaaaa-.../views
  MoNew-Request-User-ID: {userId}

Response: 200 OK
  {"viewCount":1,"viewedByMe":true}
```

**결과**: ✅ 첫 조회 시 viewCount 증가, 재조회 시 유지

#### 14. DELETE /api/articles/{articleId} (기사 논리 삭제)

```
Request:
  DELETE /api/articles/aaaaaaaa-...-000000000002

Response: 204 No Content
```

**결과**: ✅ 목록에서 제외됨

---

### comment-controller (6개 엔드포인트)

#### 15. POST /api/comments (댓글 작성)

```
Request:
  POST /api/comments
  MoNew-Request-User-ID: {userId}
  {"articleId":"aaaaaaaa-...","content":"Swagger에서 테스트합니다!"}

Response: 200 OK
  {"id":"5ac813f4-...","userNickname":"유저2","content":"Swagger에서 테스트합니다!","likeCount":0}
```

**결과**: ✅ Article의 commentCount도 연동 증가

#### 16. GET /api/comments (댓글 목록)

```
Request:
  GET /api/comments?articleId=aaaaaaaa-...

Response: 200 OK — 2건, 최신순 정렬
```

**결과**: ✅

#### 17. PATCH /api/comments/{commentId} (댓글 수정)

```
Request:
  PATCH /api/comments/5ac813f4-...
  MoNew-Request-User-ID: {작성자 userId}
  {"content":"Swagger에서 수정된 댓글입니다"}

Response: 200 OK
  content 변경, updatedAt 갱신
```

**결과**: ✅

#### 18. DELETE /api/comments/{commentId} (댓글 삭제)

```
Response: 204 No Content
```

**결과**: ✅ 논리 삭제

#### 19. POST /api/comments/{commentId}/likes (좋아요)

```
Request:
  POST /api/comments/5ac813f4-.../likes
  MoNew-Request-User-ID: {다른 userId}

Response: 200 OK
  {"likeCount":1,"likedByMe":true}
```

**결과**: ✅ 좋아요 시 댓글 작성자에게 알림 자동 생성

#### 20. DELETE /api/comments/{commentId}/likes (좋아요 취소)

```
Response: 200 OK
  {"likeCount":0,"likedByMe":false}
```

**결과**: ✅

---

### notification-controller (3개 엔드포인트)

#### 21. GET /api/notifications (미확인 알림 목록)

```
Request:
  GET /api/notifications
  MoNew-Request-User-ID: {userId}

Response: 200 OK
  {"content":[{"content":"수정된유저님이 회원님의 댓글을 좋아합니다.","confirmed":false}]}
```

**결과**: ✅ 이벤트 기반 자동 알림 생성 확인

#### 22. PATCH /api/notifications/{notificationId} (개별 알림 확인)

> USER1에게 미확인 알림이 없어 SKIP (이전 테스트에서 전체 확인 처리됨)

**결과**: - (스킵)

#### 23. PATCH /api/notifications (전체 알림 확인)

```
Request:
  PATCH /api/notifications
  MoNew-Request-User-ID: {userId}

Response: 200 OK
  {"updated":1}
```

**결과**: ✅

---

### user-activity-controller (1개 엔드포인트)

#### 24. GET /api/user-activities/{userId} (활동 내역)

```
Request:
  GET /api/user-activities/d4cae8fa-...

Response: 200 OK
  {
    "userId": "d4cae8fa-...",
    "nickname": "테스트유저",
    "subscriptions": [{"interestName":"클로드","interestKeywords":["Claude","AI assistant","Anthropic"]}],
    "recentComments": [{"articleTitle":"Claude AI 최신 업데이트","content":"Claude가 정말 좋아졌네요!"}],
    "recentCommentLikes": [],
    "recentViewedArticles": [{"articleTitle":"Claude AI 최신 업데이트","source":"TEST"}]
  }
```

**결과**: ✅ PostgreSQL 활동(구독/댓글/조회) → MongoDB document 자동 동기화 확인

USER2 활동 내역:
```
  subscriptions: [인공지능]
  recentComments: [2건]
  recentViewedArticles: [GPT-5 출시 소식]
```

**결과**: ✅

---

### article-backup-controller (2개 엔드포인트)

#### 25. GET /api/articles/backups (백업 날짜 목록)

```
Request:
  GET /api/articles/backups

Response: 200 OK
  [] (백업 실행 전이므로 빈 목록)
```

> 참고: `MONEW_BACKUP_ADMIN_TOKEN` 환경변수 미설정 시 목록 조회는 통과하지만 복구는 차단됨

**결과**: ✅

#### 26. POST /api/articles/backups/restore (백업 복구)

```
Request:
  POST /api/articles/backups/restore?date=2026-04-13

Response: 403 Forbidden
  {"code":"FORBIDDEN","message":"접근 권한이 없습니다","details":{"reason":"admin-token-not-configured"}}
```

**결과**: ✅ 토큰 미설정 시 403 차단 정상

---

## 에러 케이스 테스트

| # | 테스트 | 예상 | 실제 | 결과 |
|---|--------|------|------|------|
| 1 | 존재하지 않는 사용자 조회 | 404 | 404 `USER_NOT_FOUND` | ✅ |
| 2 | 잘못된 비밀번호 로그인 | 401 | 401 `INVALID_PASSWORD` | ✅ |
| 3 | 필수 헤더 누락 (MoNew-Request-User-ID) | 400 | 400 `MISSING_REQUEST_HEADER` | ✅ |
| 4 | 유효성 검증 실패 (빈 이메일) | 400 | 400 `VALIDATION_FAILED` (email: 공백일 수 없습니다, nickname: 크기가 2에서 50 사이여야 합니다) | ✅ |
| 5 | 중복 이메일 가입 | 409 | 409 `DUPLICATE_USER` | ✅ |
| 6 | 삭제된 사용자 조회 | 404 | 404 `USER_NOT_FOUND` | ✅ |
| 7 | 삭제된 기사 조회 | 404 | 404 `ARTICLE_NOT_FOUND` | ✅ |
| 8 | 빈 댓글 작성 | 400 | 400 `VALIDATION_FAILED` | ✅ |
| 9 | 백업 복구 (토큰 미설정) | 403 | 403 `FORBIDDEN` | ✅ |

---

## 참고: 유사 이름 관심사 등록

```
POST /api/interests
{"name":"클로드AI","keywords":["test"]}

Response: 201 Created (등록 성공)
```

> "클로드"와 "클로드AI"는 유사도 80% 미만으로 판정되어 등록이 허용됨.
> 80% 이상 유사 이름(예: "클로드" vs "클로드들")은 별도 검증 필요.

---

## 전체 결과 요약

| 컨트롤러 | 엔드포인트 수 | 성공 | 실패 | 스킵 |
|----------|:---:|:---:|:---:|:---:|
| user-controller | 5 | 5 | 0 | 0 |
| interest-controller | 6 | 6 | 0 | 0 |
| article-controller | 3 | 3 | 0 | 0 |
| comment-controller | 6 | 6 | 0 | 0 |
| notification-controller | 3 | 2 | 0 | 1 |
| user-activity-controller | 1 | 1 | 0 | 0 |
| article-backup-controller | 2 | 2 | 0 | 0 |
| **합계** | **26** | **25** | **0** | **1** |

에러 케이스: 9건 전부 정상 처리

**Swagger OpenAPI 스키마**: 22개 DTO 스키마 정의 확인

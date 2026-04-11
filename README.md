# 모뉴 (MoNew)

> **MongoDB 및 PostgreSQL 백업 및 복구 시스템** — 여러 뉴스 API/RSS를 통합 수집하고 관심사 기반으로 맞춤 뉴스를 제공하는 Spring 기반 뉴스 통합 관리 서비스.

## 기술 스택

- **Java 17**, Spring Boot 3.4, Gradle
- **PostgreSQL + MongoDB** (폴리글랏 영속성)
- Spring Batch (뉴스 수집 배치, 활동 내역 동기화)
- Spring Data JPA / Spring Data MongoDB
- MapStruct + Lombok
- AWS S3 (뉴스 기사 백업/복구)
- JUnit 5, Mockito, Jacoco (커버리지 80% 목표)

## 핵심 설계 포인트

### 6개 도메인
| 도메인 | 저장소 | 특이사항 |
|---|---|---|
| 사용자 (User) | PostgreSQL | 논리 삭제, 1일 후 완전 삭제 |
| 관심사 (Interest) | PostgreSQL | 80% 이상 유사 이름 등록 불가, 키워드 다수, 구독 |
| 뉴스 기사 (Article) | PostgreSQL | 원본 링크 unique, 논리 삭제, 조회수 unique 집계 |
| 댓글 (Comment) | PostgreSQL | 논리 삭제, 좋아요 토글 |
| **활동 내역 (Activity)** | **MongoDB** | **역정규화 읽기모델 — 선제 갱신** |
| 알림 (Notification) | PostgreSQL | 확인 후 1주일 경과 시 배치 삭제 |

### 폴리글랏 영속성
- PostgreSQL — 트랜잭션·정합성 필요 원본
- MongoDB — 읽기 최적화된 `UserActivity` document (구독 관심사 + 최근 댓글 10 + 최근 좋아요 10 + 최근 본 기사 10)
- 활동 내역은 [역정규화 읽기모델 패턴](https://) 적용. 사용자 활동 발생 시마다 선제 갱신.

### 뉴스 수집 파이프라인
1. Naver API + 한국경제/조선일보/연합뉴스 RSS 수집
2. 등록된 관심사의 키워드를 포함하는 기사만 저장
3. 원본 링크 기준 중복 제거
4. **매 시간 배치** 실행
5. 날짜 단위 S3 스냅샷 백업
6. 복구 시 S3 vs DB diff → 유실분만 재등록

### 인증
- `MoNew-Request-User-ID` 헤더로 사용자 식별
- 요청 ID/IP를 MDC로 로그·응답 헤더에 추가

## 실행

### 사전 준비
```bash
# Docker Compose로 PostgreSQL + MongoDB 실행
docker compose up -d

# .env 파일 생성
cp .env.example .env
```

### 빌드 & 실행
```bash
./gradlew clean build
./gradlew bootRun
```

### 테스트
```bash
./gradlew test
# JaCoCo 리포트: build/reports/jacoco/test/html/index.html
```

## 프로젝트 구조

```
src/main/java/com/monew/
├── MonewApplication.java
├── config/                  # AppConfig, WebMvcConfig, MDCLoggingInterceptor
├── common/
│   ├── entity/              # BaseEntity, BaseUpdatableEntity
│   └── exception/           # MonewException, ErrorCode, GlobalExceptionHandler
└── domain/
    ├── user/                # JPA
    ├── interest/            # JPA — Interest + Keyword + Subscription
    ├── article/             # JPA — Article + ArticleView
    ├── comment/             # JPA — Comment + CommentLike
    ├── activity/            # MongoDB — UserActivity document
    └── notification/        # JPA
```

각 도메인 패키지는 `entity/` (또는 `document/`), `repository/`, `service/`, `controller/`, `exception/` 하위 구조를 공유한다.

## 요구사항 구현 현황 (2026-04-11)

- ✨ 표시: 2026-04-11 P0 버그 수정 및 A2 구현에서 반영한 항목
- 🆕 표시: 2026-04-11 2차 보강(검증 리포트 PARTIAL/FAIL 해소)에서 반영한 항목

### 기능 요구사항

#### 사용자
- [x] U1 사용자 등록 (이메일 중복 검증 포함) 🆕 BCrypt 비밀번호 해싱 적용
- [x] U2 로그인 🆕 PasswordEncoder.matches 로 비교
- [x] U3 사용자 정보 조회
- [x] U4 닉네임 수정
- [x] U5 사용자 논리 삭제 🆕 deletedAt 타임스탬프 + 1일 경과 완전 삭제 배치

#### 관심사
- [x] I1 관심사 등록 (Levenshtein 80% 유사도 검증)
- [x] I2 관심사 목록 조회 🆕 이름/키워드 검색어 + 구독자수 정렬(asc/desc) 추가
- [x] I3 관심사 키워드 수정
- [x] I4 관심사 삭제
- [x] I5 관심사 구독
- [x] I6 관심사 구독 취소

#### 뉴스 기사
- [x] A1 뉴스 수집 배치 (Naver API + RSS 4종, 매 시간) ✨ RSS 소스 확장 (JTBC/연합/정책)
- [x] A2 기사 목록 필터/검색/정렬 ✨ keyword/sourceIn/publishedFrom/publishedTo/direction 지원 🆕 sortBy(PUBLISHED_AT/VIEW_COUNT/COMMENT_COUNT) 추가
- [x] A3 기사 조회수 unique 집계 ✨ 중복 조회 경쟁 상태 fallback 수정
- [x] A4 기사 논리 삭제
- [x] A5 S3(혹은 InMemory) 스냅샷 백업
- [x] A6 기사 복구 🆕 REST 엔드포인트 노출 (`GET /api/articles/backups`, `POST /api/articles/backups/restore`)

#### 댓글
- [x] C1 댓글 등록
- [x] C2 댓글 목록 조회 (커서 페이지네이션) ✨ JPQL null 파라미터 버그 수정 🆕 좋아요순 정렬 추가
- [x] C3 본인 댓글 수정
- [x] C4 본인 댓글 삭제
- [x] C5 댓글 좋아요
- [x] C6 댓글 좋아요 취소

#### 활동 내역
- [x] AC1 사용자 활동 내역 (MongoDB 역정규화 읽기모델, 이벤트 기반 선제 갱신)

#### 알림
- [x] N1 미확인 알림 목록 조회 ✨ JPQL null 파라미터 버그 수정
- [x] N2 알림 확인
- [x] N3 알림 일괄 확인
- [x] N4 확인 후 1주일 경과 자동 삭제 배치 🆕 `NotificationCleanupScheduler` 매일 00:10 cron

### 기술 요구사항

- [x] T1 Bean Validation + DTO 계층 검증
- [x] T2 전역 예외 체계 (MonewException + ErrorCode + GlobalExceptionHandler) ✨ MissingRequestHeaderException 핸들러 추가
- [x] T3 MDC 로깅 (requestId, requestMethod, requestUrl 응답 헤더 포함)
- [x] T4 JaCoCo 커버리지 80% 🆕 LINE 91% / BRANCH 81% / INSTRUCTION 90% (임계값 80%로 상향)
- [x] T5 CI 파이프라인 (GitHub Actions 빌드/테스트/커버리지) 🆕 ECS 배포 워크플로우 스켈레톤 추가 (`.github/workflows/deploy.yml`)
- [x] T6 Swagger / OpenAPI 문서
- [x] T7 Micrometer 기반 메트릭 ✨ Prometheus 레지스트리 추가 (`/actuator/prometheus`)
- [x] T8 AWS S3 자격증명 관리 (환경변수)
- [x] T9 폴리글랏 영속성 (PostgreSQL + MongoDB)
- [x] T10 BaseEntity / BaseUpdatableEntity + JPA Auditing

---

## 참고

- 요구사항 원문: Codeit 모뉴 프로젝트 브리프 (Notion)
- 자매 프로젝트: 덕후감 (Codeit 중급 Spring 프로젝트)

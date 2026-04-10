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

## 참고

- 요구사항 원문: Codeit 모뉴 프로젝트 브리프 (Notion)
- 자매 프로젝트: 덕후감 (Codeit 중급 Spring 프로젝트)

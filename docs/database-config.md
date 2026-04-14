# 데이터베이스 설정

## PostgreSQL

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 5432 |
| Database | monew |
| User | monew |
| Password | monew |
| JDBC URL | `jdbc:postgresql://localhost:5432/monew` |
| Docker 컨테이너명 | monew-postgres |
| 이미지 | postgres:16-alpine |

## MongoDB

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 27017 |
| Database | monew |
| User | monew |
| Password | monew |
| URI | `mongodb://monew:monew@localhost:27017/monew?authSource=admin` |
| Docker 컨테이너명 | monew-mongodb |
| 이미지 | mongo:7 |

## 실행 방법

```bash
# Docker Compose로 DB 실행
docker compose up -d

# DB 상태 확인
docker compose ps

# DB 중지
docker compose down

# DB 볼륨까지 삭제 (데이터 초기화)
docker compose down -v
```

## IntelliJ Database 연결

1. 우측 사이드바 **Database** 탭 클릭
2. `monew-postgres`, `monew-mongodb` 선택
3. 비밀번호 `monew` 입력
4. **Test Connection**으로 연결 확인

드라이버가 없으면 IntelliJ가 자동 다운로드를 안내한다.

## 트러블슈팅

- **`comment_count NOT NULL` 마이그레이션 오류**: docker compose 볼륨에 옛 스키마가 남아 있을 때 발생. `docker compose down -v` 후 재시작.
- **MongoDB 인증 실패**: 로컬에 인증 없는 MongoDB가 별도로 실행 중일 때 포트 충돌 가능. `SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/monew`로 오버라이드.

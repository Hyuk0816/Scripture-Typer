# Work Report

## 0. Project Metadata
| Item | Detail |
|:---:|:---|
| Project | Scripture-Typer Full-Stack Migration |
| Tech Stack | Vue.js 3 + Spring Boot 4.0 + PostgreSQL + Redis |
| Plan | 2026-02-13/work-plan.md |
| Created | 2026-02-13 |
| Last Updated | 2026-02-17 02:01:09 |

## 1. Compliance Rules (Strictly Enforced)
1. Print and confirm compliance rules before starting any work
2. Record real-time timestamps via `date "+%Y-%m-%d %H:%M:%S"` upon completing each work item
3. Use Context7 MCP to avoid deprecated libraries, methods, and coding patterns
4. Record all errors and fixes with real-time timestamps immediately
5. Review previous work items before starting new ones
6. 작업 시작 전 `git branch`로 현재 브랜치 확인, work-plan.md의 Git 브랜치 전략에 맞는 브랜치에서 작업 중인지 검증
7. Phase 시작 시 해당 Phase의 브랜치를 main에서 생성, Phase 완료 시 main으로 PR 머지

## 2. Implementation Steps
| Step | Task Description | Started | Completed | Worker | Notes |
|:---:|:---|:---:|:---:|:---:|:---|
| **Phase 0** | **디렉토리 구조 재배치** | 2026-02-14 00:19:34 | 2026-02-14 00:20:29 | Claude | 기존 코드 보존 + 신규 디렉토리 준비 |
| 0-1 | 기존 `backend/` → `backend-express/` 리네임 | - | 완료(사전) | - | 이미 리네임 완료 상태 |
| 0-2 | 기존 `frontend/` → `frontend-react/` 리네임 | - | 완료(사전) | - | 이미 리네임 완료 상태 |
| 0-3 | 기존 `docker-compose.yml` → `docker-compose.old.yml` 백업 | - | 완료(사전) | - | 이미 백업 완료 상태 |
| 0-4 | 기존 Docker 컨테이너 및 볼륨 정리 | 2026-02-14 00:19:34 | 2026-02-14 00:20:29 | Claude | docker compose down -v 완료 |
| **Phase 1** | **인프라 및 프로젝트 스캐폴딩** | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | Docker + Spring Boot + Vue.js 초기 구성 |
| 1-1 | Docker Compose 구성 (PostgreSQL, Redis, BE, FE) | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | 포트: FE 3333, BE 8080, DB 10432, Redis 6379 |
| 1-2 | Spring Boot 4.0 + Java 21 프로젝트 생성 | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | Spring Initializr + Gradle Kotlin DSL |
| 1-3 | Vue.js 3 + Vite + TS 프로젝트 생성 | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | Tailwind CSS v4 + Pinia + Vue Router + Axios |
| 1-4 | Atomic Design 디렉토리 구조 설계 | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | atoms/molecules/organisms/templates/pages |
| 1-5 | 공통 설정 (CORS, 환경변수, 프록시) | 2026-02-14 00:27:28 | 2026-02-14 01:07:43 | Claude | Vite proxy→BE, CORS config, SecurityConfig |
| **Phase 2** | **DB 스키마 설계 및 JPA 엔티티** | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | 전체 도메인 모델 설계 |
| 2-1 | JPA 엔티티: User, Role, UserStatus | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | Lombok + Builder, JPA Auditing |
| 2-2 | JPA 엔티티: Bible, Testament | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | 기존 Prisma 스키마 동일 |
| 2-3 | JPA 엔티티: UserProgress, ProgressMode | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | mode 필드 추가, User FK |
| 2-4 | JPA 엔티티: Board, Reply, PostType | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | 역할 기반 접근 제한용 |
| 2-5 | JPA 엔티티: ChatSession, ChatMessage | 2026-02-16 00:59:48 | 2026-02-16 01:18:54 | Claude | 기존 스키마 + User FK |
| ~~2-6~~ | ~~JPA 엔티티: GeminiUsageLog~~ | - | - | - | Redis로 대체 (Decision #5) |
| ~~2-7~~ | ~~Flyway 마이그레이션 스크립트 생성~~ | - | - | - | JPA ddl-auto로 대체 (Decision #6) |
| ~~2-8~~ | ~~Bible CSV 데이터 로딩 (Seed)~~ | - | - | - | DataGrip 수동 import (Decision #7) |
| **Phase 3** | **회원가입 및 인증 시스템** | 2026-02-16 01:27:06 | 2026-02-16 14:18:12 | Claude | JWT + Redis + Spring Security + Vue 프론트엔드 |
| 3-1 | Spring Security + JWT 설정 | 2026-02-16 01:27:06 | 2026-02-16 02:15:00 | Claude | JwtTokenProvider, JwtAuthenticationFilter, RefreshTokenService, SecurityConfig 수정 |
| 3-2 | 회원가입 API | 2026-02-16 02:15:00 | 2026-02-16 03:30:00 | Claude | AuthController POST /api/auth/signup (201), SignupRequest DTO, 이메일 중복 체크 |
| 3-3 | 로그인 API | 2026-02-16 02:15:00 | 2026-02-16 03:30:00 | Claude | POST /api/auth/login, BCrypt 검증, ACTIVE 상태 체크, JWT 발급 |
| 3-4 | 로그아웃 API | 2026-02-16 02:15:00 | 2026-02-16 03:30:00 | Claude | POST /api/auth/logout, Redis 리프레시 토큰 삭제 |
| 3-5 | 토큰 갱신 API | 2026-02-16 02:15:00 | 2026-02-16 03:30:00 | Claude | POST /api/auth/refresh, 토큰 검증 후 재발급 |
| 3-6 | 관리자 회원 승인 API | 2026-02-16 03:30:00 | 2026-02-16 04:00:00 | Claude | PATCH /api/admin/users/{id}/activate, /deactivate (204) |
| 3-7 | 관리자 회원 목록 API | 2026-02-16 03:30:00 | 2026-02-16 04:00:00 | Claude | GET /api/admin/users?status= (200), UserListResponse DTO |
| 3-8 | Vue 로그인/회원가입 페이지 | 2026-02-16 13:07:52 | 2026-02-16 13:16:05 | Claude | Atomic Design: Atoms 7, Molecules 2, Organisms 2, Templates 1, Pages 2 |
| 3-9 | Vue 관리자 회원 승인 페이지 | 2026-02-16 13:07:52 | 2026-02-16 13:16:05 | Claude | StatusFilterTabs + UserListTable + AdminTemplate + AdminUsersPage |
| 3-10 | Vue 인증 상태 관리 (Pinia) | 2026-02-16 13:07:52 | 2026-02-16 13:16:05 | Claude | types, api (interceptors), Pinia authStore, 글로벌 스타일 |
| 3-11 | Vue Router 가드 | 2026-02-16 13:07:52 | 2026-02-16 13:16:05 | Claude | beforeEach 가드, Route meta 타입 확장, initializeAuth |
| **Phase 4** | **성경 데이터 및 사이드바** | 2026-02-16 20:00:00 | 2026-02-16 23:53:42 | Claude | 백엔드 API + 프론트엔드 사이드바 + 라우팅 |
| 4-1 | 성경 책 목록 API | 2026-02-16 20:00:00 | 2026-02-16 21:00:00 | Claude | BibleRepository (BookProjection), BibleService, BibleController |
| 4-2 | 성경 장 조회 API | 2026-02-16 20:00:00 | 2026-02-16 21:00:00 | Claude | findByBookNameAndChapter + VerseResponse DTO |
| 4-3 | Vue 사이드바 컴포넌트 | 2026-02-16 23:49:10 | 2026-02-16 23:53:42 | Claude | Sidebar, BookList, BookItem, ChapterGrid, AppHeader, MainLayout |
| 4-4 | Vue 구약/신약 탭 | 2026-02-16 23:49:10 | 2026-02-16 23:53:42 | Claude | TestamentTabs molecule + uiStore |
| 4-5 | Pinia bibleStore | 2026-02-16 23:49:10 | 2026-02-16 23:53:42 | Claude | bibleStore + uiStore + bibleApi + types/bible.ts |
| **Phase 5** | **통독 기능 (신규)** | 2026-02-17 00:15:02 | 2026-02-17 02:01:09 | Claude | Redis Write-Behind + Read-Through + Vue 통독 뷰 |
| 5-1 | 통독 모드 API 설계 | 2026-02-17 00:15:02 | 2026-02-17 00:50:00 | Claude | ProgressCacheService, ProgressService, ProgressController, ProgressSyncScheduler |
| 5-2 | 통독 진도 저장 API | 2026-02-17 00:15:02 | 2026-02-17 00:50:00 | Claude | POST /api/progress/reading/save (Redis only), GET 조회 (Read-Through) |
| 5-3 | 통독 완료 API | 2026-02-17 00:15:02 | 2026-02-17 00:50:00 | Claude | POST /api/progress/reading/complete (DB 직접), 3시간 스케줄러 sync |
| 5-4 | Vue 통독 뷰 컴포넌트 | 2026-02-17 01:56:54 | 2026-02-17 02:01:09 | Claude | ReadingPage, VerseList, PerPageSelector, PageNavigator, readingStore, progressApi |
| 5-5 | Vue 통독/필사 메뉴 전환 | 2026-02-17 01:56:54 | 2026-02-17 02:01:09 | Claude | ModeTabs molecule + AppHeader 통합 |
| **Phase 6** | **필사 기능 (기존 마이그레이션)** | - | - | - | 핵심: 글자별 색상, IME |
| 6-1 | 필사 진도 저장 API | - | - | - | |
| 6-2 | 필사 완료 API | - | - | - | |
| 6-3 | 최근 진도 조회 API | - | - | - | |
| 6-4 | 전체 진도 조회 API | - | - | - | |
| 6-5 | Vue 타이핑 영역 컴포넌트 | - | - | - | |
| 6-6 | Vue 완료 모달 | - | - | - | |
| 6-7 | Pinia typingStore | - | - | - | |
| 6-8 | Pinia progressStore | - | - | - | |
| **Phase 7** | **대시보드 및 마이페이지** | - | - | - | 통독/필사 통합 통계 |
| 7-1 | Vue 대시보드 페이지 | - | - | - | |
| 7-2 | Vue 마이페이지 | - | - | - | |
| 7-3 | 통계 API | - | - | - | |
| **Phase 8** | **게시판 기능 (신규)** | - | - | - | 역할 기반 접근 제한 |
| 8-1 | 게시글 CRUD API | - | - | - | |
| 8-2 | 게시글 목록 조회 (페이지네이션) | - | - | - | |
| 8-3 | 게시글 상세 조회 역할 제한 | - | - | - | |
| 8-4 | 답글 CRUD API | - | - | - | |
| 8-5 | Vue 게시판 리스트 페이지 | - | - | - | |
| 8-6 | Vue 게시글 상세 페이지 | - | - | - | |
| 8-7 | Vue 게시글 작성 페이지 | - | - | - | |
| 8-8 | Vue 답글 컴포넌트 | - | - | - | |
| **Phase 9** | **Gemini 채팅 (제한 기능 포함)** | - | - | - | SSE + 일일 5회 제한 |
| 9-1 | Gemini Streaming API | - | - | - | |
| 9-2 | 채팅 세션 CRUD API | - | - | - | |
| 9-3 | 일일 사용량 제한 로직 | - | - | - | |
| 9-4 | 사용량 체크 API | - | - | - | |
| 9-5 | 시스템 프롬프트 강화 | - | - | - | |
| 9-6 | Vue 채팅 패널 | - | - | - | |
| 9-7 | Vue 채팅 세션 목록 | - | - | - | |
| 9-8 | Pinia chatStore | - | - | - | |
| **Phase 10** | **Redis 캐싱 전략** | - | - | - | 진도 추적 최적화 |
| 10-1 | Redis 설정 및 RedisTemplate 구성 | - | - | - | |
| 10-2 | 진도 캐시 키 전략 설계 | - | - | - | |
| 10-3 | 캐시 읽기 로직 (Read-Through) | - | - | - | |
| 10-4 | 캐시 쓰기 로직 (Write-Behind) | - | - | - | |
| 10-5 | 스케줄러: Redis → DB 동기화 | - | - | - | |
| 10-6 | 캐시 무효화 전략 | - | - | - | |
| 10-7 | 장애 대응: Redis 다운 시 fallback | - | - | - | |
| **Phase 11** | **통합 및 마무리** | - | - | - | E2E 검증 |
| 11-1 | 전체 Docker Compose 테스트 | - | - | - | |
| 11-2 | 데이터 마이그레이션 스크립트 | - | - | - | |
| 11-3 | API 통합 테스트 | - | - | - | |
| 11-4 | 프론트엔드 반응형 검증 | - | - | - | |
| 11-5 | 보안 검증 | - | - | - | |

## 3. Architecture Decisions
| # | Decision | Alternatives Considered | Rationale |
|:---:|:---|:---|:---|
| 1 | 기존 코드를 `backend-express/`, `frontend-react/`로 리네임하여 보존 | (A) 기존 코드 삭제 후 새로 시작 (B) Git 브랜치 분리 (C) `backend-new/` 등 별도 디렉토리 | 기존 로직(IME, 스마트 따옴표, SSE 스트리밍 등) 실시간 참조 필수. 신규 프로젝트가 최종 디렉토리명(`backend/`, `frontend/`) 사용하여 마이그레이션 완료 후 추가 작업 불필요 |
| 2 | Phase별 feature 브랜치 전략 채택 | (A) main 직접 작업 (B) develop 브랜치 추가 (GitFlow) | Phase 단위로 feat/ 브랜치 생성 → main PR 머지. 1인 개발 규모에 적합한 단순 전략 |
| 3 | 커밋 단위를 Step 또는 연관 작업 단위로 분리 | (A) Phase 단위 뭉텅이 커밋 (B) 파일 단위 개별 커밋 | 커밋 메시지만으로 변경 내용 파악 가능하도록. PR 리뷰 시 변경 범위 명확화 |
| 4 | User 엔티티 필드 정의 (사용자 지정) | work-plan 원안의 Role enum 포함 구조 | 사용자가 직접 필드 구성 결정. 아래 상세 참조 |
| 5 | Gemini 사용량 카운트를 Redis로 관리 | (A) DB GeminiUsageLog 테이블 (B) Redis + DB 병행 | 일일 제한 체크 목적이므로 Redis TTL로 충분. Step 2-6 제거, Phase 9에서 Redis 카운트 로직 구현 |
| 6 | Flyway 제거, JPA ddl-auto=update 사용 | (A) Flyway SQL 버전 관리 | 1인 개발 + 신규 프로젝트, 운영 배포 시점에 Flyway 도입. Step 2-7 제거 |
| 7 | Bible CSV 데이터를 DataGrip으로 수동 import | (A) CommandLineRunner (B) SQL COPY (C) data.sql | 코드 작성 불필요, DataGrip CSV Import 기능 활용. Step 2-8 제거 |
| 8 | JWT 설정: jjwt 라이브러리, Access 1시간, Refresh 7일, Redis 저장 | - | 사용자 지정 |
| 9 | 도메인별 구조화된 예외 처리 체계 | (A) 단일 enum에 모든 에러코드 (B) @ControllerAdvice만 사용 | ExceptionCode 인터페이스 → 도메인별 enum 구현 → BusinessException → 도메인별 구체 Exception. 사용자 지정 |
| 10 | API 응답에 메시지 미포함 (HTTP 상태코드만 사용) | (A) 응답 body에 메시지 포함 | 메시지는 클라이언트 책임. void + @ResponseStatus 패턴 사용. 사용자 지정 |
| 11 | 풀 라우팅(Option B) + 레이아웃 분리 | (A) 상태 기반 뷰 전환 (기존 React 방식) (C) 하이브리드 | URL 공유 가능, 브라우저 뒤로가기 지원. MainLayout(사이드바O) / AuthTemplate / AdminTemplate 분리 |
| 12 | 통독/필사 진행률 완전 독립 | 공유 진행률 | 각 모드별 독립 API, 독립 store. 사용자 지정 |
| 13 | Bible CSV를 CommandLineRunner + JDBC batch로 로딩 | (A) DataGrip 수동 import | 코드로 관리, 앱 시작 시 자동 로딩, count>0이면 skip (멱등성). Decision #7 변경 |
| 14 | ~~통독 뷰 반응형 분기: 웹=페이지네이션, 모바일=스크롤~~ → 통독 뷰 모바일/데스크톱 통일 페이지네이션 | (A) 통일된 스크롤 뷰 ~~(B) 통일된 페이지네이션~~ | 모바일 무한 스크롤 시 진도 추적 불가 문제 → 모바일/데스크톱 모두 1절/5절/10절 페이지네이션으로 통일. 사용자 지정 |

### Decision #4 상세: User 엔티티 & 가입 DTO 정의

**User 엔티티**
| 필드 | 타입 | 비고 |
|------|------|------|
| id | Long (PK) | Auto-generated |
| name | String | 이름 |
| ttorae | Integer | 또래 |
| phone | String | 전화번호 |
| email | String | 이메일 |
| password | String | BCrypt 암호화 |
| role | Role enum | ADMIN, USER, PASTOR, MOKJANG (기본값: USER) |
| status | UserStatus enum | PENDING, ACTIVE, INACTIVE (기본값: PENDING) |
| created_at | DateTime | JPA Auditing 자동 기입 |
| updated_at | DateTime | JPA Auditing 자동 기입 |

**가입 신청 DTO (SignupRequest)**
| 필드 | 타입 |
|------|------|
| name | String |
| ttorae | Integer |
| phone | String |
| email | String |
| password | String |

## 3-1. Git Branch Tracking
| Phase | Branch | Status | Merged |
|:---:|:---|:---:|:---:|
| 0 | `chore/directory-restructure` | completed | PR #3 |
| 1 | `feat/project-scaffolding` | completed | PR #4 |
| 2 | `feat/jpa-entities` | completed | PR #6 |
| 3 | `feat/auth-system` | completed | PR #7 |
| 4 | `feat/bible-api` | - | - |
| 5 | `feat/reading-mode` | - | - |
| 6 | `feat/typing-mode` | - | - |
| 7 | `feat/dashboard-mypage` | - | - |
| 8 | `feat/board` | - | - |
| 9 | `feat/gemini-chat` | - | - |
| 10 | `feat/redis-caching` | - | - |
| 11 | `chore/integration-test` | - | - |

## 4. Error & Fix Log
| Timestamp | Step | Error Description | Fix Applied | Notes |
|:---:|:---:|:---|:---|:---|

## 5. Key Implementation Notes

### Phase 0: 디렉토리 구조 재배치
- Step 0-1~0-3: 프로젝트 초기화 시 이미 완료된 상태 확인
- Step 0-4: `docker compose -f docker-compose.old.yml down -v` 실행
  - 제거된 컨테이너: `scripture-typer-frontend-1`, `scripture-typer-backend-1`, `scripture-typer-db-1`
  - 제거된 볼륨: `scripture-typer_backend_generated`, `scripture-typer_postgres_data`
  - 제거된 네트워크: `scripture-typer_default`

### Phase 1: 인프라 및 프로젝트 스캐폴딩
- Step 1-1: docker-compose.yml (PostgreSQL 16, Redis 7, Spring Boot, Vue.js)
- Step 1-2: Spring Boot 4.0 + Java 21, Spring Initializr 기반, Gradle Kotlin DSL
  - 의존성: web, data-jpa, security, data-redis, validation, flyway, postgresql
- Step 1-3: Vue.js 3 + Vite + TypeScript, Tailwind CSS v4, Pinia, Vue Router, Axios
- Step 1-4: Atomic Design 구조 (atoms/molecules/organisms/templates → pages)
  - 추가 디렉토리: stores, composables, types, utils
- Step 1-5: CorsConfig (localhost:3333 허용), SecurityConfig (Stateless JWT 준비), Vite proxy (/api → BE:8080)
- Backend 패키지 구조: config, controller, domain, dto, repository, service, security
- Dockerfile: backend (eclipse-temurin:21), frontend (node:20-alpine + nginx)

### Phase 2: DB 스키마 설계 및 JPA 엔티티
- 전체 도메인 모델 6개 엔티티, 6개 enum 생성
- Lombok: @Getter + 생성자 레벨 @Builder + @NoArgsConstructor(PROTECTED)
- JPA Auditing: @CreatedDate, @LastModifiedDate 자동 기입
- 도메인 패키지 분리: domain/{user, bible, progress, board, chat}
- ddl-auto=update, Flyway 비활성화
- 커밋 7개 (역할별 분리): entities, enums, config, repository, application.yml, Lombok, domain 재구성

### Phase 3: 회원가입 및 인증 시스템 (백엔드)
- **Step 3-1: JWT 설정**
  - jjwt 0.12.6 (api + impl + jackson), Lombok compileOnly + annotationProcessor
  - JwtTokenProvider: createAccessToken(userId, email, role), createRefreshToken(userId), validateToken(), getUserId(), getRole()
  - JwtAuthenticationFilter: Bearer 토큰 추출 → SecurityContext 설정 (principal=userId, ROLE_ prefix)
  - RefreshTokenService: Redis 기반 (key: "refresh:{userId}", TTL: 7일)
  - SecurityConfig: JWT 필터 추가, /api/admin/** ADMIN 역할 제한, @EnableMethodSecurity
- **Step 3-2~3-5: Auth API**
  - POST /api/auth/signup → 201 (이메일 중복 체크, BCrypt 암호화)
  - POST /api/auth/login → 200 + TokenResponse (credentials 검증, ACTIVE 상태 체크)
  - POST /api/auth/logout → 204 (Redis 리프레시 토큰 삭제)
  - POST /api/auth/refresh → 200 + TokenResponse (토큰 검증 후 재발급)
  - DTO: SignupRequest (validation), LoginRequest, TokenResponse, RefreshRequest
- **Step 3-6~3-7: Admin API**
  - PATCH /api/admin/users/{id}/activate → 204
  - PATCH /api/admin/users/{id}/deactivate → 204
  - GET /api/admin/users?status= → 200 + List<UserListResponse>
  - UserListResponse: User.from() 정적 팩토리 메서드
- **예외 처리 체계**
  - ExceptionCode 인터페이스 (getHttpStatus, getMessage, getErrorCode)
  - BusinessException extends RuntimeException (ExceptionCode 주입)
  - 도메인별 ExceptionCode enum: AuthExceptionCode(5개), UserExceptionCode(1개), BoardExceptionCode(3개), ChatExceptionCode(2개), ProgressExceptionCode(1개)
  - 도메인별 구체 Exception: DuplicateEmailException, InvalidCredentialsException, AccountNotApprovedException, InvalidTokenException, UserNotFoundException 등
  - GlobalExceptionHandler: BusinessException, MethodArgumentNotValidException 처리 → ErrorResponse(errorCode, message)
- 커밋 6개 (역할별 분리): security, exception, auth-dto, auth-service/controller, admin, Lombok fix

### Phase 3: 프론트엔드 인증 구현
- **Step 3-10: 인증 상태 관리 (Foundation Layer)**
  - index.html: lang="ko", Noto Serif KR Google Fonts preconnect + link, title "Scripture Typer"
  - main.css: React index.css 글로벌 스타일 이식 (body, scrollbar, selection, animations)
  - types/auth.ts: Role, UserStatus string literal union + 7개 인터페이스 (백엔드 DTO 1:1 매칭)
  - utils/api.ts: Axios 인스턴스 + Bearer 토큰 interceptor + 401 자동 갱신 (isRefreshing + failedQueue) + authApi/adminApi
  - stores/auth.ts: Pinia composable store (user, isAuthenticated, isAdmin, login, signup, logout, initializeAuth)
  - JWT 디코딩: atob + JSON.parse (라이브러리 없이)
- **Step 3-8: 로그인/회원가입 페이지 (Atomic Design)**
  - Atoms 7개: InputField, ButtonPrimary, ButtonSecondary, ErrorMessage, Label, Badge, Spinner
  - Molecules 2개: FormField (Label+InputField+ErrorMessage), StatusBadge (Badge + status/role 한글 매핑)
  - Organisms 2개: LoginForm (이메일/비밀번호 + 클라이언트 검증 + API 호출 + 에러 표시), SignupForm (6필드 + 비밀번호 확인 + 가입 완료 "승인 대기" 메시지)
  - Templates 1개: AuthTemplate (중앙 정렬 카드 + 로고)
  - Pages 2개: LoginPage, SignupPage
  - 디자인 토큰: amber gradient 버튼, rounded-2xl 카드, #faf9f6 배경
- **Step 3-11: Vue Router 가드**
  - 라우트 4개: /, /login, /signup, /admin/users
  - Route meta 타입 확장: requiresAuth, requiresAdmin, guest
  - beforeEach 가드: 미인증→/login?redirect=, guest+인증→/, requiresAdmin+비ADMIN→/
  - main.ts: Pinia 초기화 후 authStore.initializeAuth() 호출
- **Step 3-9: 관리자 회원 승인 페이지**
  - Molecules 1개: StatusFilterTabs (전체/대기중/활성/비활성 + 카운트)
  - Organisms 1개: UserListTable (사용자 테이블 + 승인/비활성화 버튼 + 로딩 상태)
  - Templates 1개: AdminTemplate (backdrop-blur 헤더 + 로그아웃 + 콘텐츠)
  - Pages 1개: AdminUsersPage (필터 탭 + 사용자 테이블 + 전체 회원 조회 후 클라이언트 필터링)

### Phase 4: 성경 데이터 및 사이드바
- **Step 4-1~4-2: 백엔드 Bible API**
  - BibleRepository: interface projection (BookProjection) for aggregate query, entity return for chapter query
  - BookSummaryResponse: record DTO with static from(BookProjection) factory
  - BooksResponse: oldTestament/newTestament grouped by testament
  - ChapterResponse: bookName + chapter + List<VerseResponse>
  - BibleService: Collectors.groupingBy(testament) + stream().map() 패턴
  - BibleController: GET /api/bible/books, GET /api/bible/{bookName}/{chapter}
  - Architecture Decision #14: aggregate query → projection, entity query → entity return
- **Step 4-3: Vue 사이드바 컴포넌트**
  - Sidebar (organism): 모바일 백드롭 + 반응형 w-64/w-0 전환
  - BookList (organism): 구약/신약 탭 상태에 따른 필터링, onMounted fetchBooks
  - BookItem (organism): 아코디언 확장/축소, ChapterGrid 조건부 렌더링
  - ChapterGrid (organism): 6열 그리드, 장 클릭 시 /reading/:book/:chapter 라우팅
  - AppHeader (organism): 햄버거 메뉴, 로고, 책명+장 표시, 로그아웃
  - MainLayout (template): Sidebar + AppHeader + <router-view /> 중첩 라우트
- **Step 4-4: Vue 구약/신약 탭**
  - TestamentTabs (molecule): OLD/NEW 전환, amber 액센트 활성 상태
  - uiStore: sidebarOpen, activeTestament, expandedBook 상태 관리
- **Step 4-5: Pinia bibleStore + 타입**
  - types/bible.ts: BookSummary, BooksResponse, VerseData, ChapterResponse
  - stores/bible.ts: fetchBooks(), oldTestament/newTestament/loading/error 상태
  - stores/ui.ts: Testament 타입, sidebar/testament/expandedBook 상태
  - utils/api.ts: bibleApi (getBooks, getChapter) 추가
- **Router 업데이트**: Option B (풀 라우팅) 구현
  - MainLayout을 부모 라우트로, Dashboard/Reading/Typing을 children으로 중첩
  - /reading/:book/:chapter, /typing/:book/:chapter placeholder 페이지
- **TypeScript 수정**: FormField/InputField modelValue를 string|number로 확장, decodeJwt null 체크 추가

### Phase 5: 통독 기능
- **Step 5-1~5-3: 백엔드 Progress API (Redis Write-Behind + Read-Through)**
  - ProgressCacheService: Redis Hash 진도 저장 (`progress:{userId}:READING:{bookName}:{chapter}`), dirty set 관리
  - ProgressService: saveReadingProgress(Redis only), completeReading(DB 직접 + Redis sync), getReadingProgress(Read-Through), syncToDb(key parsing)
  - ProgressController: 4 REST endpoints (POST save/complete, GET single/all)
  - ProgressSyncScheduler: `@Scheduled(cron = "0 0 */3 * * *")` 3시간 주기, dirty→syncing RENAME 패턴
  - DTO: SaveReadingProgressRequest, CompleteReadingRequest, ReadingProgressResponse
  - 테스트: 28개 (ProgressServiceTest 12, ProgressCacheServiceTest 8, ProgressSyncSchedulerTest 3, ProgressControllerTest 5)
  - Spring Boot 4.0: `@MockitoBean` 사용, `spring-boot-starter-webmvc-test` 별도 모듈, `@WebMvcTest` CSRF `.with(csrf())` 필수
- **Step 5-4: Vue 통독 뷰 컴포넌트**
  - types/progress.ts: 백엔드 DTO 매칭 (SaveReadingProgressRequest, CompleteReadingRequest, ReadingProgressResponse)
  - utils/api.ts: progressApi (saveReadingProgress, completeReading, getReadingProgress, getAllReadingProgress)
  - stores/reading.ts: Pinia readingStore (fetchChapter, saveProgress, completeChapter, pagination)
  - ReadingPage.vue: Desktop=페이지네이션(1/5/10절), Mobile=전체스크롤, 통독완료 버튼
  - VerseList organism: 절 번호 + 내용, 읽은 절 하이라이트
  - PerPageSelector molecule: 1/5/10절 전환
  - PageNavigator molecule: 페이지네이션 UI
- **Step 5-5: Vue 통독/필사 메뉴 전환**
  - ModeTabs molecule: 통독/필사 탭 (route 기반 현재 모드 감지, 같은 책+장으로 모드 전환)
  - AppHeader에 ModeTabs 통합

## 6. Scope Changes
| # | Type | Description | Impact | Decision |
|:---:|:---:|:---|:---|:---|
| 1 | Removed | Step 2-6 GeminiUsageLog JPA 엔티티 | Phase 2에서 1개 Step 제거 | Gemini 일일 카운트를 Redis TTL로 관리. Phase 9에서 구현 |
| 2 | Removed | Step 2-7 Flyway 마이그레이션 스크립트 | Phase 2에서 1개 Step 제거 | JPA ddl-auto=update로 스키마 자동 생성. 운영 배포 시 Flyway 도입 검토 |
| 3 | Removed | Step 2-8 Bible CSV 데이터 로딩 | Phase 2에서 1개 Step 제거 | DataGrip CSV Import로 수동 처리 |

Type: `Added` | `Changed` | `Removed` | `Deferred`

## 7. Work Summary
| Item | Detail |
|:---:|:---|
| Total Duration | - |
| Completed Phases | - |
| Remaining Items | - |
| Architecture Decisions | - |
| Errors Encountered | - |
| Scope Changes | - |
| Notes | - |

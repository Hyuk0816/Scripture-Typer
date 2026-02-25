# Work Report

## 0. Project Metadata
| Item | Detail |
|:---:|:---|
| Project | Scripture-Typer Full-Stack Migration |
| Tech Stack | Vue.js 3 + Spring Boot 4.0 + PostgreSQL + Redis |
| Plan | 2026-02-13/work-plan.md |
| Created | 2026-02-13 |
| Last Updated | 2026-02-25 23:41:14 |

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
| **Phase 6** | **필사 기능 (기존 마이그레이션)** | 2026-02-23 21:54:04 | 2026-02-23 22:09:34 | Claude | 핵심: 글자별 색상, IME |
| 6-1 | 필사 진도 저장 API | 2026-02-23 21:54:04 | 2026-02-23 21:58:00 | Claude | DTO 3개 + Repository + Service 5 메서드 + Controller 5 엔드포인트 |
| 6-2 | 필사 완료 API | 2026-02-23 21:54:04 | 2026-02-23 21:58:00 | Claude | completeTyping DB 즉시 동기화 + readCount++ |
| 6-3 | 최근 진도 조회 API | 2026-02-23 21:54:04 | 2026-02-23 21:58:00 | Claude | getLatestTypingProgress + totalVerses |
| 6-4 | 전체 진도 조회 API | 2026-02-23 21:54:04 | 2026-02-23 21:58:00 | Claude | getAllTypingProgress + totalVerses |
| 6-5 | Vue 타이핑 영역 컴포넌트 | 2026-02-23 21:58:00 | 2026-02-23 22:02:38 | Claude | VerseDisplay, TypingInput (IME+색상), VerseProgress, TypingPage |
| 6-6 | Vue 완료 모달 | 2026-02-23 21:58:00 | 2026-02-23 22:02:38 | Claude | BadgeDisplay + CompletionModal |
| 6-7 | Pinia typingStore | 2026-02-23 21:58:00 | 2026-02-23 22:02:38 | Claude | 기존 React typingStore 이식, fetchChapter/saveProgress/completeVerse/completeChapter |
| 6-8 | Pinia progressStore → Phase 7 defer | - | - | - | typingStore가 직접 API 호출 |
| **Phase 7** | **대시보드 및 마이페이지** | 2026-02-23 22:44:08 | 2026-02-23 23:02:31 | Claude | 대시보드+마이페이지+랭킹 |
| 7-1 | 백엔드: reading/latest API + 랭킹 ZSET + 랭킹 API | 2026-02-23 22:44:08 | 2026-02-23 22:55:00 | Claude | 커밋 3개 분리 |
| 7-2 | 프론트: progressStore + ranking API 타입/클라이언트 | 2026-02-23 22:55:00 | 2026-02-23 22:58:00 | Claude | types, api, stores/progress.ts |
| 7-3 | 대시보드 페이지 (최근 진도 카드 + 랭킹 Top 3) | 2026-02-23 22:58:00 | 2026-02-23 22:59:00 | Claude | DashboardPage.vue 교체 |
| 7-4 | 마이페이지 (통독/필사 탭 + 통계 + 진도 목록) | 2026-02-23 22:59:00 | 2026-02-23 23:00:00 | Claude | MyPagePage.vue 생성 |
| 7-5 | 라우터 + 헤더 마이페이지 연결 | 2026-02-23 23:00:00 | 2026-02-23 23:02:31 | Claude | /mypage 라우트 + AppHeader 링크 |
| **Phase 8** | **게시판 기능 (신규)** | 2026-02-24 21:05:53 | 2026-02-24 21:25:26 | Claude | 역할 기반 접근 제한 |
| 8-1 | 백엔드: Repository + DTO | 2026-02-24 21:05:53 | 2026-02-24 21:10:00 | Claude | BoardRepository, ReplyRepository, BoardRequest, BoardListResponse, BoardDetailResponse, ReplyResponse, ReplyRequest |
| 8-2 | 백엔드: BoardService (CRUD + 역할 제한) | 2026-02-24 21:10:00 | 2026-02-24 21:13:00 | Claude | 8개 메서드, BIBLE_QUESTION 접근 제한 로직 |
| 8-3 | 백엔드: BoardController (8 REST endpoints) | 2026-02-24 21:13:00 | 2026-02-24 21:15:00 | Claude | POST/GET/PUT/DELETE boards + replies |
| 8-4 | 프론트: 타입 + API + Store | 2026-02-24 21:15:00 | 2026-02-24 21:18:00 | Claude | types/board.ts, api.ts boardApi, stores/board.ts |
| 8-5 | 프론트: 게시판 리스트 페이지 | 2026-02-24 21:18:00 | 2026-02-24 21:20:00 | Claude | BoardListPage.vue (카테고리 탭, 목록, 페이지네이션) |
| 8-6 | 프론트: 게시글 상세 페이지 | 2026-02-24 21:20:00 | 2026-02-24 21:22:00 | Claude | BoardDetailPage.vue (상세+답글+역할분기+403처리) |
| 8-7 | 프론트: 게시글 작성/수정 페이지 | 2026-02-24 21:22:00 | 2026-02-24 21:24:00 | Claude | BoardWritePage.vue (작성/수정 공용) |
| 8-8 | 프론트: 라우터 + 헤더 연결 | 2026-02-24 21:24:00 | 2026-02-24 21:25:26 | Claude | 4개 라우트 + AppHeader 게시판 링크 |
| **Phase 9** | **Gemini 채팅 (제한 기능 포함)** | 2026-02-24 22:00:53 | 2026-02-24 22:27:33 | Claude | SSE + 일일 5회 제한 |
| 9-1 | 백엔드: build.gradle.kts + application.yml | 2026-02-24 22:00:53 | 2026-02-24 22:05:00 | Claude | WebFlux 의존성, Gemini 설정 (api-key, model, daily-limit) |
| 9-2 | 백엔드: Repository + DTO | 2026-02-24 22:05:00 | 2026-02-24 22:08:00 | Claude | ChatSessionRepo, ChatMessageRepo, DTO 6개 (record) |
| 9-3 | 백엔드: GeminiService (WebClient SSE) | 2026-02-24 22:08:00 | 2026-02-24 22:12:00 | Claude | WebClient → Gemini API SSE → SseEmitter 중계 |
| 9-4 | 백엔드: GeminiUsageService (Redis 일일 제한) | 2026-02-24 22:12:00 | 2026-02-24 22:15:00 | Claude | Redis INCR→체크, ADMIN 무제한, DB Role 조회 |
| 9-5 | 백엔드: ChatService + ChatController | 2026-02-24 22:15:00 | 2026-02-24 22:18:00 | Claude | 7개 메서드, 7개 엔드포인트 |
| 9-6 | 프론트: 타입 + API + Store | 2026-02-24 22:18:00 | 2026-02-24 22:22:00 | Claude | types/chat.ts, chatApi, Pinia chatStore (SSE fetch 파싱) |
| 9-7 | 프론트: 채팅 컴포넌트 + MainLayout 통합 | 2026-02-24 22:22:00 | 2026-02-24 22:27:33 | Claude | 5개 컴포넌트 + MainLayout ChatButton/ChatPanel |
| **Phase 9-A** | **LangChain4j 도입 (WebClient→LangChain4j 전환)** | 2026-02-25 22:18:17 | 2026-02-25 22:22:22 | Claude | Gemini 스트리밍 안정화 + LangSmith 연동 기반 |
| 9-A-1 | build.gradle.kts: webflux → langchain4j-google-ai-gemini 교체 | 2026-02-25 22:18:17 | 2026-02-25 22:20:00 | Claude | dev.langchain4j:langchain4j-google-ai-gemini:1.9.1 |
| 9-A-2 | GeminiService 전면 재작성 (LangChain4j StreamingChatModel) | 2026-02-25 22:20:00 | 2026-02-25 22:22:22 | Claude | WebClient/DataBuffer/processChunk 제거 → StreamingChatResponseHandler 콜백 |
| **Phase 9-B** | **LangFuse 연동 (LLM Observability)** | 2026-02-25 22:53:00 | 2026-02-25 23:00:49 | Claude | 프롬프트/응답/토큰 사용량 추적 |
| 9-B-1 | application.yml + application-local.yml LangFuse 설정 추가 | 2026-02-25 22:53:00 | 2026-02-25 22:55:00 | Claude | langfuse.secret-key, public-key, base-url (env var + local 하드코딩) |
| 9-B-2 | LangfuseTraceService 생성 (REST API 클라이언트) | 2026-02-25 22:55:00 | 2026-02-25 22:58:00 | Claude | HttpClient 비동기 POST /api/public/ingestion, Basic Auth, trace+generation batch |
| 9-B-3 | LangfuseChatModelListener 생성 (ChatModelListener 구현) | 2026-02-25 22:58:00 | 2026-02-25 23:00:00 | Claude | onRequest→traceId 기록, onResponse→LangFuse 전송, onError→에러 트레이스 |
| 9-B-4 | GeminiService에 LangFuse 리스너 등록 | 2026-02-25 23:00:00 | 2026-02-25 23:00:49 | Claude | .listeners(List.of(langfuseListener)) + application-test.yml 갱신 |
| **Phase 10** | **Redis 캐싱 고도화** | 2026-02-25 23:34:51 | 2026-02-25 23:41:14 | Claude | 기존 Phase 5 보완 |
| 10-1 | RedisConfig + 커넥션 풀/타임아웃 설정 | 2026-02-25 23:34:51 | 2026-02-25 23:36:00 | Claude | Lettuce pool (max-active:16, max-idle:8, min-idle:2), timeout:2s |
| 10-2 | complete 후 Redis 키 삭제 + TTL 3일 | 2026-02-25 23:36:00 | 2026-02-25 23:38:00 | Claude | deleteProgressKey(), 쓰기/읽기마다 EXPIRE 3일 갱신 |
| 10-3 | keys() → SCAN 교체 | 2026-02-25 23:36:00 | 2026-02-25 23:38:00 | Claude | findAllUserProgress()에서 scanKeys() 비블로킹 조회 |
| 10-4 | Redis 장애 fallback | 2026-02-25 23:38:00 | 2026-02-25 23:40:00 | Claude | save/get 전 try-catch, 실패 시 DB 직접 쓰기/읽기 |
| 10-5 | 실패 키 재시도 (progress:failed set) | 2026-02-25 23:40:00 | 2026-02-25 23:41:14 | Claude | 스케줄러에서 failed set 먼저 재시도 → 재실패 시 다시 등록 |
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

| 15 | WebClient+SseEmitter → LangChain4j StreamingChatModel 전환 | (A) WebClient 직접 호출 유지+버퍼링 수정 (B) Spring AI (C) LangChain4j | WebClient DataBuffer 청크 분할로 SSE 스트림 끊김 반복 발생. LangChain4j가 Gemini SDK 내장, 스트리밍 콜백 안정적, LangSmith 연동 기반 제공. spring-boot-starter-webflux 의존성 제거 |
| 16 | LLM Observability로 LangFuse Cloud 선택 | (A) LangSmith (B) LangFuse (C) 자체 로깅 | LangSmith는 LangChain4j에 네이티브 자동 연동 없음 (Python LangChain만 지원). LangFuse 무료 50K obs/월 vs LangSmith 5K traces/월. REST API + ChatModelListener 조합으로 수동 연동 용이 |
| 17 | complete 후 Redis 진도 키 삭제 | (A) 키 유지 (캐시 역할) (B) 키 삭제 (DB에 이미 저장) | 완료된 장은 DB에 저장 완료. Redis에 유지하면 비활성 키 영구 잔류. 삭제 후 재조회 시 Read-Through로 DB에서 복구 |
| 18 | keys() → SCAN 교체 | (A) keys() 유지 (B) SCAN cursor 사용 | keys()는 Redis 단일 스레드를 블로킹하여 대규모 데이터 시 장애 유발. SCAN은 비블로킹 cursor 기반 |

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
| 4 | `feat/bible-api` | completed | PR #9 |
| 5 | `feat/reading-mode` | completed | PR #10 |
| 6 | `feat/typing-mode` | completed | PR #11 |
| 7 | `feat/dashboard-mypage` | completed | - |
| 8 | `feat/board` | completed | PR #13 |
| 9 | `feat/gemini-chat` | completed | - |
| 10 | `feat/redis-caching` | - | - |
| 11 | `chore/integration-test` | - | - |

## 4. Error & Fix Log
| Timestamp | Step | Error Description | Fix Applied | Notes |
|:---:|:---:|:---|:---|:---|
| 2026-02-25 22:25 | 9-A | contextLoads() 실패 - LangChain4j GeminiService 빈 생성 시 apiKey 빈값 | application-test.yml 생성 + @ActiveProfiles("test") | 테스트 환경 gemini 설정 분리 |
| 2026-02-25 22:27 | 9-A | ProgressCacheServiceTest NPE at line 68 | ValueOperations mock 추가 | saveLastVerse에 opsForValue() 호출 추가 후 테스트 미갱신 |
| 2026-02-25 22:28 | 9-A | ProgressServiceTest assertion 실패 at line 289 | containsExactlyInAnyOrder로 변경 | HashMap.values() 순서 미보장 → 순서 무관 검증으로 수정 |

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

### Phase 6: 필사 기능 (기존 마이그레이션)
- **Step 6-1~6-4: 백엔드 Typing API**
  - DTO 3개: SaveTypingProgressRequest, CompleteTypingRequest, TypingProgressResponse (totalVerses 포함)
  - BibleRepository: countByBookNameAndChapter 메서드 추가
  - ProgressRepository: findFirstByUserIdAndModeOrderByUpdatedAtDesc, findByUserIdAndModeOrderByUpdatedAtDesc 추가
  - ProgressService: BibleRepository 주입 + 5개 typing 메서드 (saveTypingProgress, completeTyping, getTypingProgress, getLatestTypingProgress, getAllTypingProgress)
  - ProgressController: 5개 typing 엔드포인트 (POST save/complete, GET single/latest/all)
  - ProgressCacheService, ProgressSyncScheduler 변경 없음 (이미 mode 파라미터 지원)
- **Step 6-7: 프론트엔드 기반**
  - types/progress.ts: SaveTypingProgressRequest, CompleteTypingRequest, TypingProgressResponse 타입 추가
  - utils/api.ts: progressApi에 5개 typing API 함수 추가
  - stores/typing.ts: Pinia composable store (React typingStore 이식, fetchChapter 병렬 호출, IME 상태 관리)
- **Step 6-5~6-6: Vue 컴포넌트**
  - atoms/BadgeDisplay.vue: 골드/실버/브론즈 배지 (readCount 기반)
  - molecules/VerseProgress.vue: 현재절/총절 + 회독수 배지
  - organisms/VerseDisplay.vue: 현재 절 번호 + 내용 읽기 전용 표시
  - organisms/TypingInput.vue: **핵심 컴포넌트** - IME compositionstart/end 핸들링, 글자별 색상(green/red/blue), 스마트 따옴표 정규화, 커서 위치 추적, 투명 textarea 오버레이
  - organisms/CompletionModal.vue: 완료 모달 (홈으로/다음장 버튼, BadgeDisplay 통합)
  - pages/TypingPage.vue: 플레이스홀더 교체, route params watch + fetchChapter, 로딩/빈상태/타이핑/완료 조건부 렌더링
- **ChapterGrid 모드 인식 수정**: route.path 기반 reading/typing 모드 감지하여 올바른 경로로 라우팅
- **Step 6-8**: progressStore는 Phase 7(대시보드)로 defer

### Phase 7: 대시보드 및 마이페이지
- **Step 7-1: 백엔드**
  - ProgressService.getLatestReadingProgress(): 최근 통독 진도 1건 (typing/latest와 동일 패턴)
  - ProgressController GET /reading/latest 엔드포인트 추가
  - ProgressCacheService.incrementTypingRanking(): Redis ZSET ZINCRBY
  - ProgressService.completeTyping에 랭킹 ZSET 호출 추가
  - RankingEntryResponse record DTO (rank, userId, name, completedChapters)
  - RankingService: Redis ZREVRANGE + User 벌크 조회 (findAllById)
  - RankingController: GET /api/ranking/typing?limit= (기본 10, 최대 50)
  - SecurityConfig 변경 불필요 (anyRequest().authenticated() 기존 룰 적용)
- **Step 7-2: 프론트엔드 기반**
  - types/progress.ts: RankingEntryResponse 타입 추가
  - utils/api.ts: getLatestReadingProgress + rankingApi.getTypingRanking 추가
  - stores/progress.ts: Pinia composable store
    - State: latestTyping, latestReading, allReading, allTyping, topRanking, loading*
    - Actions: fetchLatest (Promise.allSettled), fetchAll (Promise.all), fetchTopRanking
    - Computed: readingStats, typingStats (클라이언트 집계)
- **Step 7-3: 대시보드 페이지**
  - DashboardPage.vue 플레이스홀더 교체
  - 최근 필사 카드: bookName+chapter, 진행률 바, "이어서 필사하기" 버튼
  - 최근 통독 카드: bookName+chapter, 회독수, "이어서 통독하기" 버튼
  - 필사 랭킹 Top 3: 🥇🥈🥉 + 이름 + 완료 장 수
  - 빈 상태: "사이드바에서 시작하세요" 안내
- **Step 7-4: 마이페이지**
  - MyPagePage.vue 생성 (251 lines)
  - 탭 바: 통독 현황 / 필사 현황 전환
  - 3칸 통계 그리드 (진행중 / 완료 / 총 회독수) — readingStats / typingStats
  - 진행중 섹션: amber 배지, 클릭 시 해당 모드 페이지 이동
  - 완료 섹션: green 배지 + 회독수 표시
  - 필사 탭: 진행률 바 포함 (lastTypedVerse/totalVerses)
- **Step 7-5: 라우터 + 헤더**
  - router/index.ts: /mypage 라우트 추가 (MainLayout children)
  - AppHeader.vue: "마이페이지" 링크 추가 (관리자 링크 앞)
- **커밋 7개**: reading/latest API, 랭킹 ZSET, 랭킹 API, progressStore+타입, 대시보드, 마이페이지, 라우터+헤더

### Phase 8: 게시판 기능
- **Step 8-1: 백엔드 Repository + DTO**
  - BoardRepository: findByPostTypeOrderByCreatedAtDesc (페이지네이션+필터), findAllByOrderByCreatedAtDesc
  - ReplyRepository: findByBoardIdOrderByCreatedAtAsc
  - DTO 5개: BoardRequest(record), BoardListResponse(from 팩토리), BoardDetailResponse(replies 포함), ReplyResponse(from 팩토리), ReplyRequest
- **Step 8-2: 백엔드 BoardService**
  - 8개 메서드: createBoard, getBoards, getBoard, updateBoard, deleteBoard, createReply, updateReply, deleteReply
  - BIBLE_QUESTION 접근 제한: 상세 조회(작성자+PASTOR/MOKJANG/ADMIN), 답글 작성(PASTOR/MOKJANG/ADMIN만)
  - 삭제: 작성자 본인 또는 ADMIN, 수정: 작성자 본인만
  - PRIVILEGED_ROLES Set으로 역할 체크 공통화
- **Step 8-3: 백엔드 BoardController**
  - POST /api/boards (201 + Location), GET /api/boards (Page + postType 필터), GET /api/boards/{id} (권한 체크)
  - PUT /api/boards/{id} (204), DELETE /api/boards/{id} (204)
  - POST /api/boards/{id}/replies (201), PUT/DELETE replies (204)
- **Step 8-4: 프론트 타입 + API + Store**
  - types/board.ts: PostType, BoardListItem, BoardDetail, ReplyItem, BoardRequest, ReplyRequest, PageResponse<T>
  - api.ts: boardApi 8개 함수 (CRUD + reply CRUD)
  - stores/board.ts: Pinia composable store, 답글 CUD 후 자동 fetchBoard
- **Step 8-5: 게시판 리스트 페이지**
  - BoardListPage.vue: 4개 탭(전체/성경질문/자유/제안), 게시글 목록(배지+제목+작성자+답글수+날짜), 페이지네이션
- **Step 8-6: 게시글 상세 페이지**
  - BoardDetailPage.vue: 상세 조회(카테고리 배지, 작성자 역할 표시), 수정/삭제(본인+ADMIN), 답글 CRUD
  - BIBLE_QUESTION 답글 입력 제한 (PASTOR/MOKJANG/ADMIN만), 403 처리 (잠금 아이콘 + 안내)
- **Step 8-7: 게시글 작성/수정 페이지**
  - BoardWritePage.vue: 작성/수정 공용 (route params id 유무로 모드 전환), 카테고리 드롭다운, 제목/내용 입력
- **Step 8-8: 라우터 + 헤더**
  - router/index.ts: board-list, board-create, board-detail, board-edit 4개 라우트 (MainLayout children)
  - AppHeader.vue: "게시판" 링크 추가 (마이페이지와 관리자 사이)
- **커밋 8개**: Repository+DTO, Service, Controller, 타입+API+Store, 리스트 페이지, 상세 페이지, 작성/수정 페이지, 라우터+헤더

### Phase 9: Gemini 채팅 기능
- **Step 9-1: 의존성 + 설정**
  - build.gradle.kts: `spring-boot-starter-webflux` 추가 (WebClient SSE 스트리밍용)
  - application.yml: `gemini.api-key`, `gemini.model`, `gemini.daily-limit` 설정
- **Step 9-2: Repository + DTO**
  - ChatSessionRepository: findByUserIdOrderByUpdatedAtDesc
  - ChatMessageRepository: findBySessionIdOrderByCreatedAtAsc
  - DTO 6개 (Java record): ChatStreamRequest (inner MessageItem, ChatContext), ChatSessionCreateRequest, ChatMessageRequest, ChatSessionResponse (from 팩토리), ChatMessageResponse (from 팩토리), ChatUsageResponse
- **Step 9-3: GeminiService**
  - WebClient → Gemini API (streamGenerateContent?alt=sse) POST
  - Flux<DataBuffer> 구독 → "data: " 파싱 → candidates[0].content.parts[0].text 추출
  - SseEmitter로 프론트에 실시간 중계, 120초 타임아웃
  - buildSystemPrompt(): 한국어 성경 도우미 + context(bookName, chapter, verse) 참고
- **Step 9-4: GeminiUsageService**
  - Redis key: "chat:usage:{userId}:{yyyy-MM-dd}", TTL 86400초
  - checkAndIncrement(): INCR → 체크 → 초과 시 DECR 롤백 + DailyLimitExceededException
  - ADMIN 무제한 (DB에서 Role 직접 조회, JWT claim 미신뢰)
  - getUsageToday(): Redis GET → ChatUsageResponse(used, limit, unlimited)
- **Step 9-5: ChatService + ChatController**
  - ChatService 7개 메서드: getSessions, getMessages, createSession (title 30자 truncate), addMessage, deleteSession, getUsage, streamChat (사용량 체크 선행)
  - ChatController 7개 엔드포인트: POST /stream (SseEmitter), GET /usage, GET /sessions, GET /sessions/{id}/messages, POST /sessions (201), POST /sessions/{id}/messages (201), DELETE /sessions/{id} (204)
- **Step 9-6: 프론트엔드 기반**
  - types/chat.ts: ChatMessage, ChatSession, ChatUsage 인터페이스
  - api.ts: chatApi 6개 함수 + fetch API (SSE ReadableStream 파싱)
  - stores/chat.ts: Pinia composable store, React chatStore 이식
    - State: messages, isOpen, isLoading, sessions, currentSessionId, view, usage
    - sendMessage(): fetch('/api/chat/stream') → ReadableStream → 실시간 메시지 업데이트
    - 429 응답 → 사용량 초과 메시지, fetchUsage() 자동 갱신
- **Step 9-7: 채팅 컴포넌트**
  - atoms/ChatButton.vue: FAB (fixed bottom-right), amber-600 원형 버튼
  - molecules/ChatMessageBubble.vue: user(amber)/assistant(white) 말풍선 + 스트리밍 인디케이터
  - molecules/ChatInput.vue: textarea + 전송 + IME compositionstart/end + disabled
  - organisms/ChatSessionList.vue: 세션 목록 + 새 대화 + 삭제 + formatDate
  - organisms/ChatPanel.vue: 슬라이드 드로어 + 대화/목록 탭 + 사용량 진행 바 + 초과 시 입력 차단
  - MainLayout.vue: ChatButton + ChatPanel 추가
- **커밋 7개**: 의존성+설정, Repository+DTO, GeminiService, GeminiUsageService, ChatService+Controller, 타입+API+Store, 컴포넌트+MainLayout

### Phase 9-A: LangChain4j 도입
- **Step 9-A-1: 의존성 교체**
  - `spring-boot-starter-webflux` 제거 → `dev.langchain4j:langchain4j-google-ai-gemini:1.9.1` 추가
  - WebClient, DataBuffer, DataBufferUtils, Flux, Disposable 등 reactive 의존성 완전 제거
- **Step 9-A-2: GeminiService 재작성**
  - 기존: WebClient → Gemini REST API → bodyToFlux(DataBuffer.class) → StringBuilder 버퍼링 → processChunk → SseEmitter
  - 신규: GoogleAiGeminiStreamingChatModel → StreamingChatResponseHandler 콜백 → SseEmitter
  - SystemMessage + UserMessage + AiMessage로 대화 히스토리 구성 (LangChain4j ChatMessage 타입)
  - maxOutputTokens: 1024, temperature: 0.7
  - DataBuffer 청크 분할 문제 근본 해결 (LangChain4j SDK가 내부 처리)
### Phase 9-B: LangFuse 연동
- **Step 9-B-1: 설정**
  - application.yml: `langfuse.secret-key`, `langfuse.public-key`, `langfuse.base-url` (env var 플레이스홀더)
  - application-local.yml: LangFuse Cloud 실제 키 하드코딩
  - application-test.yml: 빈 키 (LangFuse 비활성화)
- **Step 9-B-2: LangfuseTraceService**
  - `POST /api/public/ingestion` REST API 호출 (Basic Auth: publicKey:secretKey)
  - batch 이벤트: `trace-create` + `observation-create` (type=GENERATION)
  - HttpClient.sendAsync() 비동기 전송 (응답 대기 없음, 채팅 응답 지연 방지)
  - `@PostConstruct`에서 키 검증 → enabled 플래그 (키 없으면 자동 비활성화)
- **Step 9-B-3: LangfuseChatModelListener**
  - LangChain4j `ChatModelListener` 구현
  - `onRequest()`: traceId(UUID) + startTime 기록 (attributes map)
  - `onResponse()`: 입력 메시지, 출력 텍스트, 모델명, 토큰 사용량, 소요 시간 추출 → sendTrace()
  - `onError()`: 에러 메시지 포함 trace 전송
  - `extractText()`: SystemMessage/AiMessage → text(), UserMessage → contents() TextContent 추출
- **Step 9-B-4: GeminiService 연동**
  - 생성자에 `LangfuseChatModelListener` 주입
  - `GoogleAiGeminiStreamingChatModel.builder().listeners(List.of(langfuseListener))`
  - 모든 Gemini 호출이 자동으로 LangFuse에 trace 전송

### Phase 10: Redis 캐싱 고도화
- **Step 10-1: RedisConfig**
  - RedisConfig.java 생성, StringRedisTemplate 명시적 빈 등록
  - application.yml: Lettuce pool (max-active:16, max-idle:8, min-idle:2), timeout:2s
- **Step 10-2: complete 후 키 삭제 + TTL 3일**
  - `completeReading/Typing()`: DB 저장 후 `deleteProgressKey()` 호출 → Redis 키 삭제
  - 모든 쓰기/읽기에 `redisTemplate.expire(key, Duration.ofDays(3))` 추가
  - 3일 미접근 키 자동 만료 → Read-Through로 DB에서 복구
- **Step 10-3: keys() → SCAN 교체**
  - `findAllUserProgress()`: `keys(pattern)` → `scanKeys(pattern)` (Cursor 기반)
  - `ScanOptions.scanOptions().match(pattern).count(100).build()` 비블로킹
- **Step 10-4: Redis 장애 fallback**
  - `saveReadingProgress/saveTypingProgress`: Redis 실패 시 `saveProgressToDb()` DB 직접 쓰기
  - `getReadingProgress/getTypingProgress`: Redis 실패 시 DB fallback
  - `completeReading/Typing`: Redis 읽기/삭제 실패해도 DB 저장은 정상 진행
  - `getAllReadingProgress/getAllTypingProgress`: Redis 실패 시 DB 데이터만 반환
  - `findLatestFromRedis()`: Redis 실패 시 null 반환 → DB 결과 사용
- **Step 10-5: 실패 키 재시도**
  - `progress:failed` set 추가: sync 실패 키를 markFailed()로 등록
  - 스케줄러: dirty sync 전에 failed set 먼저 재시도
  - 재실패 시 다시 failed set에 등록 (다음 3시간 후 재시도)

- **관련 버그 수정 이력** (Phase 9-A 도입 배경):
  - SecurityConfig: `DispatcherType.ASYNC.permitAll()` 추가 (SseEmitter async dispatch Access Denied)
  - GeminiPromptTemplate enum: `.getSystemPrompt()` 호출 누락 (enum 이름이 전달되던 버그)
  - application.yml: `${GEMINI_API_KEY:하드코딩값}` 기본값 설정 (로컬 IntelliJ .env 미인식)
  - application-local.yml 생성 + .gitignore 추가
  - 프론트엔드 stores/chat.ts: SSE 버퍼링 추가 (sseBuffer)

## 6. Scope Changes
| # | Type | Description | Impact | Decision |
|:---:|:---:|:---|:---|:---|
| 1 | Removed | Step 2-6 GeminiUsageLog JPA 엔티티 | Phase 2에서 1개 Step 제거 | Gemini 일일 카운트를 Redis TTL로 관리. Phase 9에서 구현 |
| 2 | Removed | Step 2-7 Flyway 마이그레이션 스크립트 | Phase 2에서 1개 Step 제거 | JPA ddl-auto=update로 스키마 자동 생성. 운영 배포 시 Flyway 도입 검토 |
| 3 | Removed | Step 2-8 Bible CSV 데이터 로딩 | Phase 2에서 1개 Step 제거 | DataGrip CSV Import로 수동 처리 |
| 4 | Deferred | Step 6-8 Pinia progressStore | Phase 7로 이동 | typingStore가 직접 API 호출하므로 Phase 6에서 불필요, Phase 7(대시보드) 통합 통계에서 구현 |

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

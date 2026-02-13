# Scripture-Typer Full-Stack Migration Work Plan

> Created: 2026-02-13
> Requirements: React+Express 기반 성경 필사 앱을 Vue.js 3 + Spring Boot 4.0 기반으로 전면 리팩토링. 회원 시스템, 게시판, AI 제한, 통독/필사 분리, Redis 캐싱 추가.

## Project Overview
- **Goal**: 기존 Scripture-Typer 앱의 기술 스택 전환 및 신규 기능 확장
- **Tech Stack**:
  - Frontend: Vue.js 3, Vite, TypeScript, Tailwind CSS, Atomic Design
  - Backend: Java 21, Spring Boot 4.0, JPA, Spring Security
  - Infra: Docker, docker-compose, PostgreSQL 16, Redis
- **Key Considerations**:
  - 기존 UI/UX 최대한 유지
  - 기존 Bible 데이터(CSV 31,102절) 마이그레이션
  - 기존 사용자 진도 데이터 마이그레이션 고려
  - 한국어 기반 성경 앱 특성 (IME 처리, 스마트 따옴표 정규화 등)

---

## 디렉토리 전략

```
Scripture-Typer/
├── backend-express/   ← 기존 Express 백엔드 (참조용, 읽기 전용)
├── frontend-react/    ← 기존 React 프론트엔드 (참조용, 읽기 전용)
├── docker-compose.old.yml  ← 기존 Docker Compose 백업
├── backend/           ← 신규 Spring Boot 4.0 (Java 21)
├── frontend/          ← 신규 Vue.js 3 (Vite + Tailwind)
├── docker-compose.yml ← 신규 Docker Compose
├── BIBLE_202602082053.csv  ← 공유 데이터
└── 2026-02-13/        ← 작업 계획/리포트
```

- 기존 코드를 `*-express`, `*-react` 접미사로 보존하여 로직 참조용으로 유지
- 신규 프로젝트가 `backend/`, `frontend/`를 사용하여 최종 구조와 동일하게 유지
- 마이그레이션 완료 후 기존 `*-express`, `*-react` 디렉토리 삭제

---

## Git 브랜치 전략

### 브랜치 구조
```
main                          ← 안정 배포 브랜치 (PR 머지만 허용)
├── feat/spring-boot-init     ← Phase 1: 백엔드 스캐폴딩
├── feat/vue-init             ← Phase 1: 프론트엔드 스캐폴딩
├── feat/jpa-entities         ← Phase 2: DB 스키마
├── feat/auth-system          ← Phase 3: 인증 시스템
├── feat/bible-api            ← Phase 4: 성경 데이터 API
├── feat/reading-mode         ← Phase 5: 통독 기능
├── feat/typing-mode          ← Phase 6: 필사 기능
├── feat/dashboard-mypage     ← Phase 7: 대시보드/마이페이지
├── feat/board                ← Phase 8: 게시판
├── feat/gemini-chat          ← Phase 9: Gemini 채팅
├── feat/redis-caching        ← Phase 10: Redis 캐싱
└── chore/integration-test    ← Phase 11: 통합 테스트
```

### 접두사 규칙
| 접두사 | 용도 | 예시 |
|:---:|:---|:---|
| `feat/` | 새 기능 구현 | `feat/auth-system`, `feat/board` |
| `fix/` | 버그 수정 | `fix/jwt-token-expiry`, `fix/ime-handling` |
| `refactor/` | 코드 리팩토링 (동작 변경 없음) | `refactor/progress-service` |
| `chore/` | 설정, 빌드, 인프라 변경 | `chore/docker-compose`, `chore/ci-pipeline` |
| `docs/` | 문서 작업 | `docs/api-specification`, `docs/readme` |
| `style/` | UI/코드 스타일 변경 | `style/tailwind-theme` |
| `test/` | 테스트 추가/수정 | `test/auth-integration` |
| `hotfix/` | 긴급 수정 (main 직접 분기) | `hotfix/security-patch` |

### 워크플로우
1. `main`에서 Phase별 feature 브랜치 생성
2. 브랜치에서 작업 → 커밋 (Step 단위)
3. Phase 완료 시 `main`으로 PR 머지
4. 한 Phase 내에서 세부 분기가 필요하면 `feat/auth-system` → `feat/auth-jwt-provider` 등 하위 브랜치 가능

### 커밋 메시지 규칙
```
<접두사>: <구현 내용 요약>

예시:
feat: Spring Boot 4.0 프로젝트 초기 설정
feat: User, Role JPA 엔티티 구현
fix: JWT 토큰 만료 시 갱신 로직 수정
chore: Docker Compose Redis 서비스 추가
docs: API 명세 문서 작성
```

---

## Implementation Steps

### Phase 0: 디렉토리 구조 재배치
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 0-1 | 기존 `backend/` → `backend-express/` 리네임 | 기존 Express 코드 보존 | 참조용, 읽기 전용 |
| 0-2 | 기존 `frontend/` → `frontend-react/` 리네임 | 기존 React 코드 보존 | 참조용, 읽기 전용 |
| 0-3 | 기존 `docker-compose.yml` → `docker-compose.old.yml` 백업 | 기존 설정 보존 | 필요시 기존 앱 구동 가능 |
| 0-4 | 기존 Docker 컨테이너 및 볼륨 정리 | 충돌 방지 | docker compose down -v (기존 DB 데이터 제거) |

### Phase 1: 인프라 및 프로젝트 스캐폴딩
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 1-1 | Docker Compose 구성 (PostgreSQL, Redis, backend, frontend) | `docker-compose.yml` 완성 | 포트: FE 3333, BE 8080, DB 10432, Redis 6379 |
| 1-2 | Spring Boot 4.0 + Java 21 프로젝트 생성 | `backend/` Gradle 프로젝트 | Spring Web, JPA, Security, Redis, Validation 의존성 |
| 1-3 | Vue.js 3 + Vite + TS 프로젝트 생성 | `frontend/` 프로젝트 | Tailwind CSS v4, Vue Router, Pinia, Axios |
| 1-4 | Atomic Design 디렉토리 구조 설계 | `atoms/`, `molecules/`, `organisms/`, `templates/`, `pages/` | 기존 컴포넌트 매핑 정의 |
| 1-5 | 공통 설정 (CORS, 환경변수, 프록시) | 개발 환경 구동 확인 | Vite proxy → Spring Boot |

### Phase 2: DB 스키마 설계 및 JPA 엔티티
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 2-1 | JPA 엔티티 설계: User, Role | `User` 엔티티 + `Role` enum (ADMIN, USER, PASTOR, MOKJANG) | status 필드: PENDING, ACTIVE, INACTIVE |
| 2-2 | JPA 엔티티 설계: Bible | `Bible` 엔티티 | testament, bookName, bookOrder, chapter, verse, content |
| 2-3 | JPA 엔티티 설계: UserProgress | `UserProgress` 엔티티 | userId → User FK, mode (READING/TYPING), lastTypedVerse, readCount |
| 2-4 | JPA 엔티티 설계: Board, Reply | `Board`, `Reply` 엔티티 | postType (BIBLE_QUESTION, FREE, SUGGESTION), role-based 접근 |
| 2-5 | JPA 엔티티 설계: ChatSession, ChatMessage | `ChatSession`, `ChatMessage` 엔티티 | 기존 스키마 유지, User FK 추가 |
| 2-6 | JPA 엔티티 설계: GeminiUsageLog | `GeminiUsageLog` 엔티티 | userId, usedAt, 일별 카운트 조회용 |
| 2-7 | Flyway 마이그레이션 스크립트 생성 | `V1__init.sql` | DDL + 인덱스 정의 |
| 2-8 | Bible CSV 데이터 로딩 (Seed) | CommandLineRunner 또는 별도 배치 | BIBLE_202602082053.csv 파싱, HTML 태그 제거 |

### Phase 3: 회원가입 및 인증 시스템
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 3-1 | Spring Security + JWT 설정 | `SecurityConfig`, `JwtTokenProvider` | Access/Refresh Token, Redis 저장 |
| 3-2 | 회원가입 API (POST /api/auth/signup) | `AuthController.signup()` | 가입 시 status=PENDING, 비밀번호 BCrypt |
| 3-3 | 로그인 API (POST /api/auth/login) | `AuthController.login()` | JWT 발급, Redis에 refresh token 저장 |
| 3-4 | 로그아웃 API (POST /api/auth/logout) | `AuthController.logout()` | Redis에서 토큰 삭제 |
| 3-5 | 토큰 갱신 API (POST /api/auth/refresh) | `AuthController.refresh()` | Refresh → 새 Access Token |
| 3-6 | 관리자 회원 승인 API (PATCH /api/admin/users/:id/activate) | `AdminController.activateUser()` | ADMIN 역할만 접근 가능 |
| 3-7 | 관리자 회원 목록 API (GET /api/admin/users) | `AdminController.getUsers()` | 상태별 필터링 (PENDING, ACTIVE) |
| 3-8 | Vue 로그인/회원가입 페이지 | `LoginPage.vue`, `SignupPage.vue` | 기존 디자인 톤 유지 (amber, gray) |
| 3-9 | Vue 관리자 회원 승인 페이지 | `AdminUserApprovalPage.vue` | 가입 신청 목록, 승인/거절 버튼 |
| 3-10 | Vue 인증 상태 관리 (Pinia) | `authStore.ts` | JWT 토큰 관리, 로그인 상태, 역할 기반 라우팅 |
| 3-11 | Vue Router 가드 (인증/역할 체크) | `router/guards.ts` | 미인증 → 로그인, PENDING → 대기 화면 |

### Phase 4: 성경 데이터 및 사이드바
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 4-1 | 성경 책 목록 API (GET /api/bible/books) | `BibleController.getBooks()` | 구약/신약 분류, 총 장수 포함 |
| 4-2 | 성경 장 조회 API (GET /api/bible/{bookName}/{chapter}) | `BibleController.getChapter()` | 절 목록 + 진도 정보 |
| 4-3 | Vue 사이드바 컴포넌트 | `Sidebar.vue`, `BookList.vue`, `ChapterGrid.vue` | Atomic: organisms |
| 4-4 | Vue 구약/신약 탭 | `TestamentTabs.vue` | Atomic: molecules |
| 4-5 | Pinia bibleStore | `bibleStore.ts` | 기존 Zustand bibleStore 이전 |

### Phase 5: 통독 기능 (신규)
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 5-1 | 통독 모드 API 설계 | 읽기 전용 구절 조회 + 진도 저장 | mode='READING' 구분 |
| 5-2 | 통독 진도 저장 API (POST /api/progress/reading/save) | `ProgressController.saveReadingProgress()` | 절 단위 진도 기록 |
| 5-3 | 통독 완료 API (POST /api/progress/reading/complete) | `ProgressController.completeReading()` | readCount 증가 |
| 5-4 | Vue 통독 뷰 컴포넌트 | `ReadingView.vue` | 성경 구절 표시, 스크롤 읽기, 완료 버튼 |
| 5-5 | Vue 통독/필사 메뉴 전환 | 네비게이션에 통독/필사 탭 추가 | Header 또는 Sidebar |

### Phase 6: 필사 기능 (기존 마이그레이션)
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 6-1 | 필사 진도 저장 API (POST /api/progress/typing/save) | `ProgressController.saveTypingProgress()` | 기존 /api/progress/save 이전 |
| 6-2 | 필사 완료 API (POST /api/progress/typing/complete) | `ProgressController.completeTyping()` | 기존 /api/progress/complete 이전 |
| 6-3 | 최근 진도 조회 API (GET /api/progress/latest) | `ProgressController.getLatest()` | 모드별 분리 또는 통합 |
| 6-4 | 전체 진도 조회 API (GET /api/progress/all) | `ProgressController.getAll()` | 통독/필사 구분 포함 |
| 6-5 | Vue 타이핑 영역 컴포넌트 | `TypingArea.vue`, `VerseDisplay.vue`, `TypingInput.vue` | 핵심: 글자별 색상, IME 처리, 스마트 따옴표 정규화 |
| 6-6 | Vue 완료 모달 | `CompletionModal.vue` | 기존 UX 유지 |
| 6-7 | Pinia typingStore | `typingStore.ts` | 기존 Zustand typingStore 이전 |
| 6-8 | Pinia progressStore | `progressStore.ts` | 기존 Zustand progressStore 이전, 모드 구분 추가 |

### Phase 7: 대시보드 및 마이페이지
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 7-1 | Vue 대시보드 페이지 | `DashboardPage.vue`, `StatsCard.vue`, `ContinueCard.vue` | 통독/필사 통계 분리 표시 |
| 7-2 | Vue 마이페이지 | `MyPage.vue`, `ReadingStatus.vue` | 통독/필사 진행 현황 |
| 7-3 | 통계 API (GET /api/progress/stats) | `ProgressController.getStats()` | 통독 수, 필사 수, 총 회독수 등 |

### Phase 8: 게시판 기능 (신규)
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 8-1 | 게시글 CRUD API | `BoardController` | POST, GET list, GET detail, PUT, DELETE |
| 8-2 | 게시글 목록 조회 (GET /api/boards) | 페이지네이션, 카테고리 필터 | postType: BIBLE_QUESTION, FREE, SUGGESTION |
| 8-3 | 게시글 상세 조회 역할 제한 | BIBLE_QUESTION → 작성자 + PASTOR/MOKJANG/ADMIN만 | Spring Security @PreAuthorize |
| 8-4 | 답글 CRUD API | `ReplyController` | BIBLE_QUESTION 답글 → PASTOR/MOKJANG/ADMIN만 |
| 8-5 | Vue 게시판 리스트 페이지 | `BoardListPage.vue` | 카테고리 탭, 게시글 목록, 페이지네이션 |
| 8-6 | Vue 게시글 상세 페이지 | `BoardDetailPage.vue` | 본문 + 답글 목록 (역할 제한 반영) |
| 8-7 | Vue 게시글 작성 페이지 | `BoardCreatePage.vue` | 카테고리 선택, 제목, 내용 |
| 8-8 | Vue 답글 컴포넌트 | `ReplySection.vue`, `ReplyItem.vue` | 역할별 답글 가능 여부 표시 |

### Phase 9: Gemini 채팅 (제한 기능 포함)
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 9-1 | Gemini Streaming API (POST /api/chat/stream) | `ChatController.streamChat()` | SSE, Spring WebFlux 또는 SseEmitter |
| 9-2 | 채팅 세션 CRUD API | `ChatHistoryController` | 기존 세션/메시지 API 이전 |
| 9-3 | 일일 사용량 제한 로직 | `GeminiUsageService` | ADMIN 제외, 하루 5회 제한 |
| 9-4 | 사용량 체크 API (GET /api/chat/usage) | `ChatController.getUsage()` | 오늘 사용 횟수 / 남은 횟수 |
| 9-5 | 시스템 프롬프트 강화 (성경 질문 필터) | 시스템 프롬프트에 성경 외 질문 거부 로직 | 추후 상세 논의 예정 |
| 9-6 | Vue 채팅 패널 | `ChatPanel.vue`, `ChatMessage.vue`, `ChatInput.vue` | 기존 UX 유지, 사용량 표시 추가 |
| 9-7 | Vue 채팅 세션 목록 | `ChatSessionList.vue` | 기존 UX 유지 |
| 9-8 | Pinia chatStore | `chatStore.ts` | 기존 Zustand chatStore 이전, 사용량 상태 추가 |

### Phase 10: Redis 캐싱 전략 (필사/통독 진도)
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 10-1 | Redis 설정 및 RedisTemplate 구성 | `RedisConfig.java` | Spring Data Redis, Jackson serializer |
| 10-2 | 진도 캐시 키 전략 설계 | `progress:{userId}:{mode}:{bookName}:{chapter}` | mode = READING or TYPING |
| 10-3 | 캐시 읽기 로직 (Read-Through) | `ProgressCacheService` | Redis 먼저 조회 → miss 시 DB 조회 → Redis 저장 |
| 10-4 | 캐시 쓰기 로직 (Write-Behind) | `ProgressCacheService` | 절 완료 시 Redis만 업데이트 (DB 미반영) |
| 10-5 | 스케줄러: Redis → DB 동기화 | `ProgressSyncScheduler` | @Scheduled, 주기적으로 dirty 키 스캔 → DB upsert |
| 10-6 | 캐시 무효화 전략 | TTL, 장 완료 시 즉시 DB 반영 | 장 완료 = 확정 이벤트, 즉시 DB 기록 |
| 10-7 | 장애 대응: Redis 다운 시 fallback | DB 직접 접근 fallback | CircuitBreaker 패턴 고려 |

### Phase 11: 통합 및 마무리
| Step | Task Description | Expected Deliverable | Notes |
|:---:|:---|:---|:---|
| 11-1 | 전체 Docker Compose 테스트 | 4개 서비스 동시 기동 확인 | DB, Redis, Backend, Frontend |
| 11-2 | 데이터 마이그레이션 스크립트 | 기존 PostgreSQL → 신규 스키마 변환 | user_progress, chat_session 등 |
| 11-3 | API 통합 테스트 | 주요 시나리오별 E2E 검증 | 회원가입→승인→로그인→필사→채팅 |
| 11-4 | 프론트엔드 반응형 검증 | 모바일/태블릿/데스크톱 확인 | 기존 sm: breakpoint 유지 |
| 11-5 | 보안 검증 | JWT 만료, CORS, XSS, SQL Injection | Spring Security 기반 |

---

## Deliverables Checklist
- [ ] Docker Compose (PostgreSQL + Redis + Spring Boot + Vue.js)
- [ ] Spring Boot 4.0 백엔드 (Java 21, JPA, Security)
- [ ] Vue.js 3 프론트엔드 (Atomic Design, Pinia, Tailwind)
- [ ] 회원가입/로그인/로그아웃 (JWT + Redis)
- [ ] 관리자 회원 승인 시스템
- [ ] 성경 데이터 로딩 및 조회 API
- [ ] 통독 기능 (읽기 모드)
- [ ] 필사 기능 (타이핑 모드, 기존 UX 유지)
- [ ] 대시보드 및 마이페이지
- [ ] 게시판 (역할 기반 접근 제한)
- [ ] Gemini 채팅 (일일 제한, SSE 스트리밍)
- [ ] Redis 캐싱 (진도 추적 최적화)
- [ ] 데이터 마이그레이션 스크립트
- [ ] 통합 테스트 및 보안 검증

---

## 기존 코드베이스 분석 요약

### 현재 아키텍처 (→ `backend-express/`, `frontend-react/`에 보존)
```
frontend-react/ (React 18 + Zustand + Tailwind + Vite)
  ├── 5 Stores: bible, typing, progress, chat, ui
  ├── 30+ Components: dashboard, typing, chat, sidebar, mypage, common
  └── API Client: localStorage UUID 기반 사용자 식별

backend-express/ (Express.js + TypeScript + Prisma + PostgreSQL)
  ├── 4 Controllers: bible, progress, chat, chatHistory
  ├── 4 Route Sets: /api/bible, /api/progress, /api/chat
  ├── 4 Models: Bible, UserProgress, ChatSession, ChatMessage
  └── Gemini API: SSE 스트리밍 응답
```

### 핵심 보존 로직
1. **글자별 색상 피드백**: 타이핑 시 정답(green), 오답(red), 조합중(blue)
2. **IME 처리**: compositionstart/end 이벤트로 한글 입력 안정화
3. **스마트 따옴표 정규화**: 유니코드 따옴표 → ASCII 따옴표 변환
4. **진도 이력**: lastTypedVerse + readCount 조합으로 진행/완료/재독 추적
5. **Gemini SSE 스트리밍**: 실시간 청크 파싱, 프론트엔드 점진적 표시

### 마이그레이션 시 주의사항
- 기존 `localStorage UUID` → 회원 시스템 전환 (데이터 마이그레이션 필요)
- `UserProgress`에 mode(READING/TYPING) 필드 추가
- 게시판, AI 제한 등 신규 기능은 기존 데이터 없음
- Bible CSV 시딩 로직 Java로 재작성 필요

I have enough to write a thorough, verified report. Let me compile it now.

---

# Foundation Review — Nettdetektivene (Sprint 1, 2026-04-20)

---

## A. Foundation Verdict

The skeleton is *solid enough to build Sprint 2 on*, with one caveat: two structural issues — a live authorization gap in a legacy controller, and a CI pipeline that skips all backend tests — must be closed before Sprint 2 adds more surface area on top of them. The domain model, DTO/layer separation, security architecture, and API client layer are all sound. The team clearly understands what they're building and has made deliberate choices. Fix the five items in section B this week, and Sprint 2 will be working on a trustworthy foundation.

---

## B. Top 5 "Fix Before Sprint 2"

*#1 — Backend CI runs zero tests* (HIGH`/Critical for NFR`)
backend/src/main/java/ has substantial test code, but the CI build is:
run: ./mvnw --batch-mode package -DskipTests
Every push to dev bypasses all tests. Change to:
run: ./mvnw --batch-mode verify
If the MySQL dependency is the blocker, set up @DataJpaTest with H2 in-memory for that step.

*#2 — Teacher can delete any classroom* (HIGH)
ClassroomController at /classrooms has PATCH /{id} and DELETE /{id} with @PreAuthorize("hasRole('TEACHER')") but *no ownership check*. Any authenticated teacher can delete any other teacher's classroom. The correct implementation is in TeacherClassroomController. Either remove ClassroomController entirely (the frontend already uses /teacher/classrooms) or add the principal check:
@PreAuthorize("hasRole('TEACHER')")
@DeleteMapping("/{id}")
public void deleteClassroom(@PathVariable UUID id,
    @AuthenticationPrincipal JwtAuthenticatedPrincipal principal) {
    classroomAuthorizationService.verifyAccess(principal, id);
    classroomService.deleteClassroom(id);
}

*#3 — S3 credentials and cookie-secure flag hardcoded in application.properties*
backend/src/main/resources/application.properties:11-15:
properties
s3.access-key=minioadmin
s3.secret-key=minioadmin
app.cookie.secure=false
These should be env-var-driven:
properties
s3.access-key=${S3_ACCESS_KEY}
s3.secret-key=${S3_SECRET_KEY}
app.cookie.secure=${APP_COOKIE_SECURE:false}

*#4 — Frontend teacher routes have auth guards removed*
frontend/src/routes/index.tsx:98: comment reads *"NOTE: <RequireAuth role="TEACHER"> wrappers removed during dev"*. Routes /teacher, /teacher/* pass through TeacherLayout but reach real API endpoints without a STUDENT/unauthenticated user being redirected. Re-add the wrappers before Sprint 2 adds more teacher pages.

**#5 — No migration tool; ddl-auto=update + SchemaFixRunner hack**
backend/src/main/resources/application.properties:8: spring.jpa.hibernate.ddl-auto=update. The SchemaFixRunner (config/SchemaFixRunner.java) already shows the team hit a wall: Hibernate's update can't drop NOT NULL, so they patched it with raw JDBC on startup. Switch to Flyway now, while the schema is still small. Add flyway-core to pom.xml, set spring.flyway.enabled=true, and export the current schema as V1__init.sql. The SchemaFixRunner can then be deleted.

---

## C. Findings by Section

### 1. Repo Layout & Onboarding

| #   | Severity | Location                          | Problem                                                                                                                                                     | Suggested fix                                                              |
| --- | -------- | --------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| 1.1 | Nit      | README.md                       | 7 lines — only describes mvn spring-boot:run and npm run dev. No mention of required env vars, DB setup, or MinIO. A new teammate is stuck immediately. | Add a "Prerequisites" section listing DB, .env file template, and MinIO. |
| 1.2 | Low      | .github/workflows/node.js.yml:1 | Workflow named node.js.yml covers Java too; misleading to faglærere reviewing CI.                                                                         | Rename to ci.yml.                                                        |
| 1.3 | Medium   | .github/workflows/node.js.yml:9 | CI only triggers on dev. PRs to main are not checked.                                                                                                   | Add main to branches.                                                  |
| 1.4 | Low      | Root Makefile                   | Makefile was deleted (git status shows D Makefile) — any documentation referencing it is stale.                                                           | Restore or remove references.                                              |

### 2. Backend Architecture

| #   | Severity | Location                                                        | Problem                                                                                                                                                                                                                            | Suggested fix                                                                                                             |
| --- | -------- | --------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| 2.1 | Low      | ClassroomController.java vs TeacherClassroomController.java | Two controllers with overlapping POST /classrooms, GET /classrooms/{id}, PATCH /classrooms/{id}, DELETE /classrooms/{id}. Frontend only uses /teacher/classrooms. The legacy controller creates confusion and auth gaps. | Delete ClassroomController entirely.                                                                                    |
| 2.2 | High     | application.properties:8                                      | ddl-auto=update with no Flyway/Liquibase. Schema changes are unversioned and non-reproducible across dev machines. SchemaFixRunner is the first sign of fallout.                                                               | Add Flyway, delete SchemaFixRunner.                                                                                     |
| 2.3 | Medium   | config/GameSeeder.java                                        | Only Nyhetskvartalet (1 of 7 stops) is seeded. The map renders but 6 stations have no content, making the seeded "sample data" spec incomplete.                                                                                  | Seed at minimum one question per remaining stop, or document the gap.                                                     |
| 2.4 | Medium   | game/service/GameService.java:48                              | Lock/unlock logic hardcoded to all-unlocked: gameMapper.toStopOverviewResponse(stop, false). LevelCompletion table exists and is populated but never consulted for locking.                                                    | Before Sprint 2 adds stations, hook LevelCompletionRepository.countByEnrollmentId here so progression works end-to-end. |
| 2.5 | Nit      | game/service/QuestionService.java:78                          | submitAnswer validates but does not record completion. The LevelCompletion entity exists and TeacherClassroomService queries it. There must be a separate completion endpoint — but it's not visible in GameController.    | Verify a completion-recording endpoint exists; if not, add it to GameController.                                        |
| 2.6 | Low      | ClassroomAuthorizationService.java:19                         | Javadoc says STUDENT: must be registered as a teacher for the classroom — obvious copy-paste error.                                                                                                                              | Fix the comment.                                                                                                          |

### 3. Backend Security

| #   | Severity     | Location                             | Problem                                                                                                                                                                  | Suggested fix                                                                              |
| --- | ------------ | ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------ |
| 3.1 | *Critical* | ClassroomController.java:36,43     | PATCH /{id} and DELETE /{id} require TEACHER role but no ownership check. Teacher A can delete Teacher B's classroom.                                              | See item B#2 above.                                                                        |
| 3.2 | Medium       | application.properties:11-15       | MinIO credentials (minioadmin`/minioadmin`) and app.cookie.secure=false hardcoded. These should never be in a committed file.                                        | Move to env vars (see B#3).                                                                |
| 3.3 | Low          | config/SecurityConfig.java:103     | CORS allowedOrigins hardcoded to http://localhost:5173. Will silently break if frontend is deployed elsewhere.                                                       | Drive via ${ALLOWED_ORIGINS:http://localhost:5173}.                                      |
| 3.4 | Low          | api/client.ts:17                   | Access token stored in localStorage. XSS can steal it. The refresh token is HttpOnly (safe), but the access token window (5 min) is still a risk for a children's app. | Accept as a known tradeoff OR reduce access token lifetime further. Document the decision. |
| 3.5 | Low          | security/JwtService.java (missing) | No explicit minimum key-length guard for JWT_SECRET. A short secret at dev time would compile and run.                                                                 | Add a startup check: if (secret.length() < 64) throw new IllegalStateException(...).     |
| 3.6 | Nit          | UserService.java:78                | findByEmail + passwordEncoder.matches — timing is acceptable (BCrypt constant-time compare). No issue.                                                               | ✅ Good.                                                                                   |
| 3.7 | Nit          | Injection                            | JPA repository + Spring Data throughout. No string-concatenated JPQL or native SQL visible.                                                                              | ✅ Clean.                                                                                  |

### 4. Frontend Architecture

| #   | Severity | Location                              | Problem                                                                                                                                                                                                                                                                 | Suggested fix                                                                                                                                      |
| --- | -------- | ------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| 4.1 | High     | routes/index.tsx:98,93,174          | Teacher routes (/teacher, /teacher/*) have RequireAuth *removed* with a dev note. Also /profile, /game, /avatar have no auth guard at all. A STUDENT can navigate to /teacher/classroom/:id — the API call fails with 403, but the page breaks visibly. | Re-add <RequireAuth role="TEACHER"> to all teacher routes. Add <RequireAuth> to /profile, /game.                                           |
| 4.2 | Medium   | hooks/useAuth.ts:16 + 4 other files | useEffect used in useAuth.ts, ConfettiComp.tsx, GamePage.tsx, Alert-Dialog.tsx, LevelOnePage.tsx. Project rules explicitly forbid it.                                                                                                                       | Replace with TanStack Query subscriptions or custom event listeners that don't require useEffect. At minimum, document why each use is approved. |
| 4.3 | Low      | App.tsx                             | Vite boilerplate — React/Vite logo counter app — is still the entry for the App component. The actual app is wired through main.tsx`→RouterProvider`. App.tsx is dead code.                                                                                       | Delete App.tsx or repurpose it.                                                                                                                  |
| 4.4 | Low      | frontend/src/api/client.ts:3        | VITE_API_URL ?? 'http://localhost:8080/api' — the ?? fallback means a missing env var silently uses localhost. Project rules say env vars must be asserted.                                                                                                         | if (!import.meta.env.VITE_API_URL) throw new Error('VITE_API_URL not set').                                                                      |
| 4.5 | Nit      | TypeScript                            | Zero any in .tsx files ✅. teacherService.ts has typed raw interfaces. Good.                                                                                                                                                                                      | Keep doing this.                                                                                                                                   |
| 4.6 | Nit      | Accessibility                         | App.tsx:14: <img ... alt=""> (empty alt on hero image — acceptable for decorative). Other pages checked have semantic HTML. RequireAuth doesn't trap focus — may need aria-live for SPA navigation. Worth a pass with a screen reader before Sprint 3.          | Defer to sprint dedicated to WCAG review.                                                                                                          |

### 5. Frontend ↔ Backend Contract

| #   | Severity | Location                                    | Problem                                                                                                                                                                                                                                                                                                                                                    | Suggested fix                                                                                                                    |
| --- | -------- | ------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| 5.1 | Medium   | services/teacherService.ts + backend DTOs | Types are manually duplicated in the frontend (RawClassroomSummary, RawMysterySubmission, etc.). SpringDoc (springdoc-openapi-starter-webmvc-ui) is in pom.xml — OpenAPI JSON is generated. But there's no type-generation step (e.g. openapi-typescript). When Sprint 2 adds fields on the backend, the frontend raw types will silently drift. | Add openapi-typescript codegen to the frontend build, consuming Swagger JSON from backend. This catches drift at compile time. |
| 5.2 | Low      | api/client.ts:39-42                       | On refresh failure, localStorage.removeItem('token') then window.location.assign(loginPath). Hard navigation drops any in-flight TanStack Query state. Acceptable but choppy UX.                                                                                                                                                                       | Fine for Sprint 2; flag for Sprint 3.                                                                                            |

### 6. Domain Model vs Requirements

| V.1 Story                        | Backend support                         | Gap                                                                                                   |
| -------------------------------- | --------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| Opprette bruker (teacher)        | ✅ /auth/register with TEACHER role | —                                                                                                     |
| Feide login (student)            | ✅ /auth/feide simulated              | —                                                                                                     |
| Bli med i klasserom              | ✅ /classrooms/join                   | Student can only join one classroom (soft limit via existsByUserAndLeftAtIsNull) ✅                 |
| Kart (stops)                     | ✅ /game/stops                        | All stops returned unlocked — see 2.4                                                                 |
| Stoppested / Oppgave             | ✅ Seeded for Nyhetskvartalet           | Missing for 6 other stops                                                                             |
| Fullført oppgave                 | ⚠️ Partial                              | submitAnswer validates but recording completion is unclear                                          |
| Medaljer                         | ⚠️ Partial                              | AvatarItem`/UserInventoryItem` is the gamification entity; dedicated badge/medal endpoint not found |
| Notatblokk                       | ❌ Missing                              | No entity, no endpoint. Page shows "Kommer snart".                                                    |
| Ledertavle                       | ❌ Missing                              | No backend endpoint. Page shows "Kommer snart".                                                       |
| Profil                           | ⚠️ AvatarController exists            | ProfilePage.tsx — unconfirmed if wired                                                              |
| Teacher: Opprette klasserom      | ✅ /teacher/classrooms POST           | —                                                                                                     |
| Teacher: Klasseromsstyring       | ✅ Students, mystery, notifications     | —                                                                                                     |
| Teacher: Ukens mysterium approve | ✅ PATCH /{id}/mystery/{submissionId} | —                                                                                                     |

*V.2 flexibility:*
- Teacher-authored tasks: Question.optionContent is a JSON blob — adding a source flag (PREDEFINED / TEACHER) to Level or Question would be enough.
- Per-classroom task variation: The join between Enrollment and LevelCompletion already scopes per-student, but questions aren't yet classroom-scoped. A classroom_level_override join table would support this without breaking the current model.
- Level-based adaptive algorithm: LevelCompletion has timestamps and a unique constraint. The foundation is there; a scoring service on top is straightforward.

### 7. Testing Foundation

| #   | Severity     | Location                           | Problem                                                                                                                                                                                                                                                                                                                                                                          |
| --- | ------------ | ---------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 7.1 | *Critical* | .github/workflows/node.js.yml:83 | -DskipTests means backend coverage is 0% in CI. Actual coverage unknown.                                                                                                                                                                                                                                                                                                       |
| 7.2 | High         | ClassroomControllerTest.java     | Empty class — placeholder with no tests.                                                                                                                                                                                                                                                                                                                                         |
| 7.3 | Medium       | Backend tests overall              | @WebMvcTest slice tests exist for TeacherClassroomController (20+ good tests), TeacherStopsController, TeacherNotificationsController, ImageController. Unit tests for OptionParser, OptionValidator. Missing: ClassroomService, UserService, GameService, QuestionService, GameController. Estimated coverage: ~20-30%. Well below the 50% requirement. |
| 7.4 | Medium       | Frontend                           | Tests cover teacherService functions via mocked API client. Zero component/page tests. Frontend coverage is probably ~25-35% across lines — close to the 30% threshold but only because the tested service is well-covered.                                                                                                                                                    |
| 7.5 | Medium       | Integration tests                  | No Testcontainers or H2 profile visible. All backend tests use @WebMvcTest (slice, no real DB). The 50% requirement says "mix of unit/integration/E2E, choice justified". Without at least documented justification for no integration tests, the grader may flag this.                                                                                                        |
| 7.6 | Nit          | frontend/src/test/smoke.test.ts  | 1 + 1 === 2 smoke test. Fine as a sanity check.                                                                                                                                                                                                                                                                                                                                |

### 8. CI / GitHub

| #   | Severity | Location         | Problem                                                                                                                                                |
| --- | -------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 8.1 | Critical | node.js.yml:83 | Backend tests skipped. See B#1.                                                                                                                        |
| 8.2 | Medium   | node.js.yml    | No coverage report step — no junit`/jacoco` XML artifact uploaded, no Vitest coverage output. Project brief requires test reports visible in GitHub. |
| 8.3 | Low      | node.js.yml:8  | CI only on dev. Main branch has no protection check.                                                                                                 |

### 9. Code Quality

| #   | Severity | Location                               | Problem                                                                                                                                                                                                       |
| --- | -------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 9.1 | Low      | game/service/GameService.java:37,48  | Inline comments // needs to set locked/unlocked later and // hgardcoded all unlocked — these are fine as breadcrumbs but should become tracked issues, not code comments.                                 |
| 9.2 | Low      | TeacherClassroomService.java:62-75   | listClassrooms builds ClassroomSummaryResponse from a raw Object[] row (row[0], row[1], etc.). If the query columns reorder, this silently produces wrong data. Use a projection interface instead. |
| 9.3 | Nit      | TeacherClassroomService.java:162,178 | Notification messages hard-coded in English: "completed a level in", "submitted a mystery answer in". App is Norwegian.                                                                                   |
| 9.4 | Nit      | Various                                | Seven contributors, generally consistent style. Lombok used uniformly. Code is readable.                                                                                                                      |

### 10. Documentation / Wiki

| Requirement           | Status  | Evidence                                                                 |
| --------------------- | ------- | ------------------------------------------------------------------------ |
| Krav (user stories)   | Partial | Vision doc exists locally (docs/vision-document.local.md), not in wiki |
| Domenemodell          | ✅      | docs/wiki/Domenemodell.md — Mermaid diagram, good quality              |
| Brukertester/modeller | Missing | No wiki page                                                             |
| Arkitektur            | Missing | No wiki page                                                             |
| Prosjektstruktur      | Missing | No wiki page                                                             |
| Servertjenester       | Missing | No wiki page                                                             |
| Databasemodell        | Partial | Covered by Domenemodell diagram; no separate ER diagram                  |
| Kildekode             | Missing | No wiki page                                                             |
| Installasjon          | Partial | Covered in README.md but sparse                                          |
| CI og testing         | Missing | No wiki page                                                             |
| Sikkerhet             | Missing | No wiki page (OWASP requirement must be documented)                      |

---

## D. NFR Compliance Checklist

| Requirement                | Status     | Evidence                                           |
| -------------------------- | ---------- | -------------------------------------------------- |
| Spring Boot REST backend   | ✅ Met     | pom.xml + controllers                            |
| MySQL persistence          | ✅ Met     | application.properties, mysql-connector-j      |
| Seed data on startup       | ⚠️ Partial | GameSeeder seeds 1 of 7 stops                    |
| Accessed only via REST     | ✅ Met     | No direct DB access from frontend                  |
| React SPA                  | ✅ Met     | main.tsx → RouterProvider                      |
| Backend ≥50% test coverage | ❌ Missing | CI skips tests; ~20-30% estimated                  |
| Frontend ≥30% coverage     | ⚠️ Partial | Service functions covered, zero component coverage |
| Chrome + Firefox support   | Unknown    | Not verified by CI                                 |
| WCAG 2.1 Principle 1       | Unknown    | Not documented; not tested in CI                   |
| Auth + authz implemented   | ✅ Met     | JWT, BCrypt, @PreAuthorize                       |
| OWASP A03 Injection        | ✅ Met     | Spring Data JPA, no raw SQL concatenation          |
| XSS                        | ⚠️ Partial | Access token in localStorage is mild XSS risk      |
| GitHub Actions CI          | ⚠️ Partial | Exists; backend tests skipped; no reports          |
| Test reports in GitHub     | ❌ Missing | No artifact upload step                            |

---

## E. What the Team Is Doing Well

*Authorization architecture* — ClassroomAuthorizationService (game/service/ClassroomAuthorizationService.java) is a clean, reusable ownership check called consistently throughout TeacherClassroomController and TeacherClassroomService. This pattern is exactly right and should be the template for every resource guard in Sprint 2.

*JWT + cookie design* — The access-token-in-header + refresh-token-in-HttpOnly-cookie architecture is textbook. The createRefreshCookie method in UserController.java:98 correctly sets httpOnly, sameSite, path, and maxAge. The token rotation on refresh (refreshTokenRepository.delete(refreshToken)) prevents reuse. This is production-quality security code for a student project.

*DTO layer discipline* — No JPA entity is ever returned from a controller. Every endpoint has a dedicated response DTO. The GameMapper goes further by using a sealed OptionResponse hierarchy to strip answer data from the question responses — a genuinely clever design that prevents correct answers from leaking to the client.

**TeacherClassroomControllerTest** (backend/src/test/.../TeacherClassroomControllerTest.java) — 20+ well-structured @WebMvcTest cases covering happy path, 401 unauthenticated, 403 wrong role, 403 wrong owner, and 404 not found. This is the right pattern. Copy this structure for every new controller in Sprint 2.

**teacherService.ts API boundary** — Raw response types are defined locally and not exported. The public-facing Classroom types are decoupled from the API shape. When the backend changes a field name, only one file needs updating. This is exactly the discipline needed to survive two more sprints of parallel frontend/backend work.

**JoinCodeGenerator** (classroom/util/JoinCodeGenerator.java) — Uses SecureRandom, strips confusable characters (O/0/I/1), checks uniqueness against the database, and fails loudly after 20 attempts. A rare example of a small utility that's gotten all the details right.

*TanStack Query setup* (main.tsx) — staleTime: 5 * 60 * 1000 is a sensible default. The query client is at the root, and the router is `RouterProvider`-based, allowing loader-level data fetching. This sets up Sprint 2 for efficient server state without waterfalls.

---

## F. Open Questions (Couldn't Resolve from Code Alone)

1. *Is there a "complete level" endpoint?* submitAnswer validates but does not write LevelCompletion. Either there's a separate POST /game/levels/{id}/complete not visible in GameController, or this is missing. The teacher dashboard queries LevelCompletion for progress tracking, so if it's never written, progress is always zero.

2. *What is the NTNU runner's DB setup?* Backend CI (build-backend) skips tests. If the self-hosted runner has a real MySQL, tests could run with a test profile. Confirm what the runner has before deciding whether Testcontainers or H2 is needed for CI.

3. **Is ClassroomController (/classrooms) intentionally kept alive?** The frontend only calls /teacher/classrooms. If ClassroomController is leftover from an earlier design, delete it. If it's intentional (public read?), add the missing ownership checks.

4. *How is the simulated Feide login supposed to work in V.2?* The current FeideFake page creates a student account. In V.2 with real school integration, does the user model need an external ID field? Adding it now is a one-column migration.

5. *Avatar unlock conditions* — AvatarItem.unlockCondition is a String column. In V.2 when the adaptive algorithm unlocks items, how is this field interpreted? A free-text string is fragile. An enum or structured JSON would be more reliable.
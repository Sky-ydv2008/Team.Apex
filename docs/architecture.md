# Architecture — Apex Innovators

Product principle: **every project should tell a story** — problem → idea → technology →
development → hackathon → result → learnings.

## 1. Product vision

A recognizable digital home for Apex Innovators that:

- showcases projects to judges, recruiters, collaborators and visitors;
- keeps a permanent archive of hackathons and innovation journeys;
- highlights team members, skills and achievements;
- grows into an authenticated community platform (posts, comments, likes, events).

The repository implements the **MVP** defined in the implementation plan §24 and is
structured so the community layer and analytics can evolve without rework.

## 2. Target users & permissions

| User | Needs | Access |
|------|-------|--------|
| Guest | Discover projects, hackathons, team, achievements | Public read-only (GET on public endpoints) |
| `MEMBER` | Profile, posts, project submissions, comments, likes | Authenticated writes on own content |
| `CORE_MEMBER` | Manage team content and community activity | Extended project/post management |
| `ADMIN` | Moderation, users, projects, analytics | Everything incl. `/api/admin/**`, audit |

## 3. Technology stack

| Layer | Choice |
|-------|--------|
| Backend | Java 21, Spring Boot 3.3.5, Spring MVC, Spring Data JPA / Hibernate 6, Spring Security |
| Auth | JWT (jjwt 0.12, HS256/384), BCrypt, stateless sessions |
| API docs | springdoc-openapi (Swagger UI at `/swagger-ui.html`) |
| Database | MySQL 8 (utf8mb4); schema owned by `database/schema.sql` (`ddl-auto: none`) |
| Frontend | HTML5, CSS3, vanilla ES modules, fetch API — no frameworks, no build step |
| Build | Maven (backend), static files (frontend) |

### Why this stack
The team's existing IntelliERP work already targets Java/Spring Boot/JWT/MySQL/vanilla JS
with REST + Swagger. Reusing that foundation keeps one language set across team projects
and matches the hackathon reference architecture (Controller → Service → Repository → DB).

## 4. Repository structure

```
apex-innovators/
├── backend/
│   ├── pom.xml                          # Spring Boot 3.3.5, Java 21
│   └── src/main/
│       ├── java/com/apexinnovators/
│       │   ├── ApexInnovatorsApplication.java
│       │   ├── audit/                   # AuditService — records admin actions
│       │   ├── config/                  # OpenAPI, CORS, SmokeDataSeeder (smoke profile)
│       │   ├── controller/              # 13 REST controllers (see docs/api.md)
│       │   ├── dto/                     # records: requests (validated) + responses
│       │   ├── entity/                  # JPA entities for all 20 tables
│       │   ├── exception/               # ApiException + global handler
│       │   ├── repository/              # Spring Data repositories
│       │   ├── security/                # JwtService, JwtFilter, SecurityConfig, principals
│       │   ├── service/                 # business logic + assemblers
│       │   └── util/
│       └── resources/
│           ├── application.yml          # env-driven config (DB_*, JWT_SECRET)
│           └── application-smoke.yml    # H2 MySQL-mode profile for local runs/tests
├── frontend/
│   ├── *.html                           # 14 public pages
│   ├── admin/*.html                     # 7 admin pages
│   ├── css/                             # style, components, responsive, dashboard
│   └── js/                              # ES modules (api, auth, components + page scripts)
├── database/
│   ├── schema.sql                       # canonical MySQL schema
│   └── seed.sql                         # first content + demo role accounts
└── docs/
```

## 5. Backend design

### Package responsibilities
- `controller` — thin HTTP layer: mapping, validation trigger, `@AuthenticationPrincipal` user.
- `service` — workflow rules (visibility, ownership, moderation state machine, audit).
- `repository` — Spring Data derived queries only.
- `entity` — flat, schema-faithful mapping (snake_case columns, relations by FK id).
- `security` — HS256/384 JWT with `type=access` (15 min) / `type=refresh` (7 days) claims;
  `JwtFilter` loads the user by email and rejects non-`ACTIVE` accounts.

### Content status semantics
Projects and posts share a state machine implemented in the service layer:

```
DRAFT ──submit──▶ PENDING_REVIEW ──admin──▶ APPROVED ──publish──▶ PUBLISHED
                        │                        │
                        └──── admin ── REJECTED ─┘
```

- Public GETs expose only `APPROVED` and `PUBLISHED`.
- Drafts/pending are visible to their author, project members, and admins — never to
  anonymous guests (404, no existence leak).
- Every admin mutating call is appended to `audit_logs` and surfaced on the dashboard.

### Security model
- Stateless; CSRF off; CORS restricted to configured origins (`app.cors.allowed-origins`).
- `BCryptPasswordEncoder` for passwords; secrets via env (`JWT_SECRET`, `DB_*`).
- Role rules: `/api/admin/**` = `ADMIN`; member writes = ownership checks in services.
- Global `@RestControllerAdvice` returns `{status, message, timestamp, path}` (+ field
  errors on 400) — same shape everywhere including 401/403 JSON from the security layer.

## 6. Frontend design

- **Design system**: dark futuristic developer aesthetic (see plan §7) — Inter +
  Space Grotesk, cyan/violet accent on deep navy surfaces, rounded cards, restrained motion.
- **Shared modules**: `api.js` (fetch wrapper: `/api` base, Bearer injection, error
  normalization, 401 → login redirect on protected calls), `auth.js` (session storage +
  guards), `components.js` (nav/footer injection, toasts, loading/empty/error renderers,
  pagination).
- **Every data page renders four states**: loading skeleton → data → empty state
  (intentional copy) → error state with retry.
- No build step: ES modules loaded directly; production deploys the folder behind the
  same origin as `/api` (reverse proxy), which is how the dev static server is used too.

## 7. Future roadmap (post-MVP, plan §25)

GitHub API integration, advanced search, developer matching, AI project summaries and
recommendations, community reputation, public API for selected content, event
registration, notifications — the schema already carries the tables
(`events`, `event_registrations`, `notifications`, `bookmarks`) for these.

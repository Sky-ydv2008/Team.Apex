# Apex Innovators — Backend (Spring Boot REST API)

Spring Boot 3.3 / Java 21 / Maven REST backend for the Apex Innovators platform:
project showcase, hackathon archive, team page, achievements, community posts,
events, contact messages, public stats/search and an ADMIN back office.

Base URL: `http://localhost:8080/api` · Swagger UI: `http://localhost:8080/swagger-ui.html`
(OpenAPI JSON at `http://localhost:8080/v3/api-docs`)

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8.0+ running locally

## 1. Create the database

From the repository root, apply the schema and the seed data in order:

```bash
mysql -u root -p apex_innovators < database/schema.sql
mysql -u root -p apex_innovators < database/seed.sql
```

`schema.sql` creates the `apex_innovators` database itself, so a fresh run is:

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p apex_innovators < database/seed.sql
```

The schema is authoritative — Hibernate runs with `ddl-auto=none` and never
touches tables.

## 2. Configure environment variables

All settings have documented development defaults; override as needed:

| Env var       | Default                                                                                                | Purpose                              |
|---------------|--------------------------------------------------------------------------------------------------------|--------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/apex_innovators?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` | MySQL JDBC URL                   |
| `DB_USER`     | `root`                                                                                                 | MySQL user                           |
| `DB_PASSWORD` | *(empty)*                                                                                              | MySQL password                       |
| `JWT_SECRET`  | `apex-dev-secret-change-me-before-production-0123456789abcdef` (dev only, ≥ 32 bytes)                  | HS256 signing key — **change in prod** |

## 3. Run

```bash
mvn spring-boot:run
```

The API listens on port `8080`. Swagger UI: <http://localhost:8080/swagger-ui.html>.

CORS is enabled for local development origins (default
`http://localhost:5500`, `http://127.0.0.1:5500`, `http://localhost:8080`).

## Default dev login

`database/seed.sql` seeds the admin account

```
apex.innovator.team@gmail.com
```

with the development password documented in that file (BCrypt-hashed).
Other member accounts are created through `POST /api/auth/register`.

## Auth model (summary)

- `POST /api/auth/register` and `/login` return `{token, refreshToken, user}`
  — access JWT (HS256, ~15 min, `type=access`) and refresh JWT (~7 days,
  `type=refresh`, opaque string, no DB table).
- `GET /api/auth/me`, member mutations and everything under `/api/admin/**`
  must send `Authorization: Bearer <access-token>`.
- `POST /api/auth/refresh` exchanges a refresh token for a new access token.
- Public content visibility rule: public reads only expose projects/posts with
  status `APPROVED` or `PUBLISHED`. Admins can read and write any status.
- Role rules: `ADMIN` only under `/api/admin/**`; project editing is allowed
  for project members / `CORE_MEMBER` / `ADMIN`; post editing and submission
  are limited to the author.
- Every admin mutating action appends an `audit_logs` row (shown under
  `GET /api/admin/overview` as `recentActivity`).

## Layout

```
src/main/java/com/apexinnovators
├── ApexInnovatorsApplication.java
├── audit      AuditService (audit_logs inserts)
├── config     CorsConfig/CorsProperties, OpenApiConfig
├── controller REST controllers (/api, incl. /api/admin/**)
├── dto        Request/response records (exact JSON field names per contract)
├── entity     JPA entities (1:1 with database/schema.sql tables) + enums
├── exception  ApiException, GlobalExceptionHandler, error shape
├── repository Spring Data JPA repositories
├── security   JwtService, JwtFilter, SecurityConfig, CustomUserDetailsService, UserPrincipal
├── service    Auth, Project, Hackathon, Team, Achievement, Post, Comment,
│              Event, Contact, Search, Stats, Admin services + DTO assemblers
└── util       Slugify, PageUtil (page/size normalization)
```

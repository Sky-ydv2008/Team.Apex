# REST API — Apex Innovators

Base URL `/api`. JSON everywhere. OpenAPI/Swagger UI: `/swagger-ui.html` (docs at
`/v3/api-docs`). All list endpoints paginate with `?page=` (0-based) & `?size=` (default
12, max 100) and return the envelope:

```json
{ "content": [], "page": 0, "size": 12, "totalElements": 0, "totalPages": 0 }
```

Errors always use:

```json
{ "status": 401, "message": "Authentication required", "timestamp": "...", "path": "/api/..." }
```

Validation failures (400) add `errors: [{ field, message }]`.

## Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | — | `{name,email,password}` → 201 `{token, refreshToken, user}`; creates `MEMBER` + profile row |
| POST | `/auth/login` | — | `{email,password}` → `{token, refreshToken, user}` |
| POST | `/auth/refresh` | — | `{refreshToken}` → `{token}` |
| GET | `/auth/me` | Bearer | current `{id,name,email,role,status}` |

Send `Authorization: Bearer <token>`. Access token lives 15 min, refresh 7 days.

## Public (no auth) — GET unless noted

| Path | Description |
|------|-------------|
| `/projects?q=&tech=&year=&featured=&page=&size=` | published projects; `featured=true` filters; `q` searches title/tagline/description; `tech` = technology name |
| `/projects/{id-or-slug}` | project detail (drafts 404 for guests) |
| `/hackathons?page=&size=` / `/hackathons/{id-or-slug}` | hackathon archive |
| `/team` | active team members (`ADMIN`/`CORE_MEMBER`) with profiles |
| `/achievements?page=&size=` | achievements with member names |
| `/posts?type=&page=&size=` / `/posts/{id}` | published community posts; `type` = `DISCUSSION\|PROJECT\|ACHIEVEMENT\|HACKATHON\|RESOURCE\|QUESTION\|ANNOUNCEMENT` |
| `/posts/{id}/comments` | comments on a published post |
| `/events?page=&size=` | upcoming events |
| `/technologies` | technology catalog (filter vocabulary) |
| `/public/stats` | `{projects, hackathons, members, technologies, achievements}` counts |
| `/search?q=` | `{projects:[], hackathons:[], posts:[]}` — published only, ≤10 per bucket |
| POST `/contact` | `{name,email,subject,message}` → 201 (stored, status `NEW`) |

## Member (Bearer)

| Method | Path | Rules |
|--------|------|-------|
| POST | `/projects` | creates `DRAFT`; acting user auto-added as project member |
| PUT | `/projects/{id}` | project member / `CORE_MEMBER` / `ADMIN`; cannot change status/slug/featured |
| POST | `/projects/{id}/submit` | author/member → `PENDING_REVIEW` |
| POST | `/posts` | author creates `DRAFT` post |
| PUT | `/posts/{id}` | author only |
| POST | `/posts/{id}/submit` | author → `PENDING_REVIEW` |
| POST | `/posts/{id}/comments` | `{body}` on approved/published post |
| POST/DELETE | `/posts/{id}/like` | toggle like; 204, idempotent |

## Admin (`/api/admin/**`, Bearer + `ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/overview` | widget totals + `recentActivity` (audit feed) |
| GET | `/projects?q=&status=&page=&size=` | all projects, any status |
| POST/PUT/DELETE | `/projects`, `/projects/{id}` | full control incl. slug/status/featured/technologies/members |
| PATCH | `/projects/{id}/status` | `{status}` moderation |
| GET/POST/PUT/DELETE | `/hackathons[...]` | hackathon CRUD |
| GET | `/users?q=&page=&size=` | member search |
| GET | `/users/{id}` | user + profile detail |
| PATCH | `/users/{id}` | `{role?, status?}` |
| GET | `/posts?status=&page=&size=` | all posts |
| PATCH | `/posts/{id}/status` | `{status}` moderation |
| GET/POST/PUT/DELETE | `/achievements[...]` | achievement CRUD |
| GET | `/messages?status=&page=&size=` | contact inbox |
| PATCH | `/messages/{id}` | `{status}` = `NEW\|READ\|REPLIED` |
| GET/POST/PUT/DELETE | `/technologies[...]` | catalog CRUD |

Every admin mutation is audited to `audit_logs` and appears in `/overview` →
`recentActivity` (`{actor, action, entity, entityId, detail, createdAt}`).

## DTO reference (JSON field names — frontend depends on these)

```jsonc
// project
{ "id":1, "title":"IntelliERP", "slug":"intellierp", "tagline":"…", "description":"…",
  "problem":"…", "solution":"…", "status":"PUBLISHED", "featured":true,
  "githubUrl":"…", "demoUrl":null, "docsUrl":null, "year":2026,
  "technologies":[{"id":1,"name":"Java","category":"Language","icon":"…"}],
  "members":[{"userId":2,"name":"Core Builder","role":"Backend Developer","contribution":"…"}],
  "hackathons":[{"id":1,"name":"Build With 2.0"}], "createdAt":"…" }

// hackathon
{ "id":1, "name":"Build With 2.0", "slug":"build-with-2.0", "organizer":null, "date":null,
  "description":"…", "challenge":"…", "result":"…", "certificateUrl":null,
  "presentationUrl":null, "members":[{"userId":2,"name":"Core Builder"}],
  "projects":[{"id":1,"title":"IntelliERP","slug":"intellierp"}] }

// member (team page)
{ "id":2, "name":"Core Builder", "role":"CORE_MEMBER", "headline":"…", "bio":"…",
  "photoUrl":null, "github":null, "linkedin":null }

// achievement
{ "id":1, "userId":2, "userName":"Core Builder", "title":"…", "type":"CERTIFICATE",
  "issuer":"…", "awardDate":"2026-09-01", "description":"…", "verifyUrl":null }

// post / comment
{ "id":1, "authorId":2, "authorName":"Core Builder", "type":"ANNOUNCEMENT", "title":"…",
  "body":"…", "status":"PUBLISHED", "createdAt":"…", "commentCount":1, "likeCount":2,
  "likedByMe":false }
{ "id":1, "authorId":2, "authorName":"Core Builder", "body":"…", "createdAt":"…" }

// event
{ "id":1, "title":"…", "date":"…", "mode":"ONLINE|OFFLINE|HYBRID", "location":null, "description":"…" }

// contact message (admin)
{ "id":1, "name":"…", "email":"…", "subject":"…", "message":"…", "status":"NEW|READ|REPLIED", "createdAt":"…" }

// auth
{ "token":"…", "refreshToken":"…", "user":{"id":1,"name":"…","email":"…","role":"ADMIN","status":"ACTIVE"} }
```

## Status & role enums

- `Role`: `ADMIN`, `CORE_MEMBER`, `MEMBER`
- `UserStatus`: `ACTIVE`, `SUSPENDED`
- `ProjectStatus` (projects & posts): `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`, `PUBLISHED`
- `MessageStatus`: `NEW`, `READ`, `REPLIED`

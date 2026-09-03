# Deployment — Apex Innovators

Reference topology from the implementation plan §22:

```
Internet ──► Domain (HTTPS)
                │
                ├── static frontend   (frontend/ — HTML/CSS/JS)
                └── /api/* ──► Spring Boot API  ──► MySQL 8
                                   (backend jar)      └── backups / monitoring
```

The frontend calls same-origin `/api` (see `frontend/js/api.js`), so production places
the static folder and the API behind one origin — a reverse proxy (nginx/Caddy/cloud
load balancer) routes `/api/*` to the Spring Boot port and everything else to the static
files. Local development mirrors this: the dev static server proxies `/api` → `:8080`
(see the root README).

## Environment variables (backend)

| Variable | Default (dev) | Required in prod |
|----------|---------------|------------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/apex_innovators?...` | yes |
| `DB_USER` | `root` | yes |
| `DB_PASSWORD` | *(empty)* | yes |
| `JWT_SECRET` | documented dev fallback | **yes — ≥ 32 random bytes**, rotate on compromise |

Never commit real credentials. `.gitignore` excludes `.env*` and local override files.

## CORS

Allowed origins come from `app.cors.allowed-origins` in `application.yml`
(defaults: `http://localhost:5500`, `http://127.0.0.1:5500`, `http://localhost:8080`).
In production, either serve everything same-origin (recommended) or list the exact
frontend origin.

## Build & run

```bash
# API jar
cd backend && mvn clean package            # → target/apex-innovators-backend-1.0.0.jar
java -jar target/apex-innovators-backend-1.0.0.jar

# local-only, no MySQL
java -jar target/apex-innovators-backend-1.0.0.jar --spring.profiles.active=smoke

# frontend — copy the folder to your static host / CDN
```

## Go-live checklist (plan §27)

- [ ] Real team profiles replace demo seed accounts; passwords rotated
- [ ] `JWT_SECRET`, `DB_*` set in the environment (never in the repo)
- [ ] Schema applied from `database/schema.sql`, then `seed.sql` once
- [ ] HTTPS enabled; HSTS header; CORS trimmed to the real origin
- [ ] Swagger reachable at `/swagger-ui.html` (or restricted to admins if desired)
- [ ] Database backup job configured; logs shipped to monitoring
- [ ] Homepage + key pages verified on desktop and mobile
- [ ] Admin login + moderation flows verified in production
- [ ] 404 / error pages checked; SEO metadata present (already per-page)
- [ ] Staging environment promoted before each production release

## Operational notes

- Schema migrations are applied as SQL DDL files (schema.sql is the source of truth;
  JPA `ddl-auto=none`). Treat schema changes as additive migrations going forward.
- `audit_logs` grows with admin activity — archive periodically.
- The H2 `smoke` profile is for local runs and CI smoke tests only; it is not a
  production substitute for MySQL.
- Contact messages and submissions start as `NEW`/`PENDING_REVIEW` — wire email/push
  notification hooks (schema already has `notifications`) when ops tooling exists.

# Apex Innovators — Team Platform

A living digital home for the **Apex Innovators** student developer team (Parul Institute of Engineering and Technology): a **portfolio + hackathon archive + project showcase + community platform** where every project tells a story — idea → research → build → hackathon → result → learnings.

**Live preview (GitHub Pages, no backend):** https://sky-ydv2008.github.io/Team.Apex/ — the
public site renders built-in sample data (see `frontend/js/demo-data.js`) so the portfolio,
hackathon archive and community feed can be viewed anywhere. Accounts, posting and the
admin panel require the full stack below. The frontend auto-switches to demo mode when no
API is present (GitHub Pages / `file://` / `?demo=1`).

**Full app on Render (accounts + admin, one-click):** this repo ships a `render.yaml`
blueprint and `backend/Dockerfile` (frontend and API in one image, linked Postgres,
demo content seeded on first boot). Deploy at https://render.com → **New+ → Blueprint** →
connect this repository → **Apply**. Default login after deploy: `apex.innovator.team@gmail.com`
/ `Apex@2000` (see `database/seed.sql`; change it in the admin panel afterwards).

Built from the full implementation plan (see `docs/architecture.md`) as an MVP:

- **Public website** — Home, Projects, Project Details, Hackathons, Hackathon Details, Team, Achievements, Community feed, Events, Resources, Contact.
- **Backend** — Spring Boot REST API with Spring Security (JWT), Spring Data JPA/Hibernate, MySQL, Swagger/OpenAPI.
- **Admin** — dashboard with live stats and activity feed, full CRUD + moderation for projects, hackathons, users, posts, achievements, contact messages, technologies.
- **Roles** — `ADMIN`, `CORE_MEMBER`, `MEMBER`, plus read-only guests. Public content is only `APPROVED`/`PUBLISHED`; drafts and pending submissions are never leaked.

## Repository layout

```
apex-innovators/
├── backend/          # Spring Boot 3.3 REST API (Java 21, Maven)
├── frontend/         # HTML5 / CSS3 / vanilla ES modules (no build step)
├── database/
│   ├── schema.sql    # MySQL 8 schema — all tables, FKs, indexes
│   └── seed.sql      # first content: IntelliERP case study + Build With 2.0 archive
└── docs/             # architecture.md · api.md · database.md · deployment.md
```

## Quick start

Prerequisites: **Java 21**, **Maven 3.9+**, **MySQL 8**.

```bash
# 1. Database (creates apex_innovators DB)
mysql -u root -p < database/schema.sql
mysql -u root -p apex_innovators < database/seed.sql

# 2. Backend — http://localhost:8080 (Swagger UI at /swagger-ui.html)
cd backend
export DB_URL=jdbc:mysql://localhost:3306/apex_innovators?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
export DB_USER=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=change-me-to-a-random-secret-of-at-least-32-bytes
mvn spring-boot:run

# 3. Frontend — serve the static site and proxy /api to :8080
#    (any static server works; the API is CORS-open for http://localhost:5500)
cd frontend
python3 -m http.server 5500      # or: npx serve, or your reverse proxy
```

**No MySQL handy?** Boot the API in-memory against H2 (MySQL mode) with a demo dataset:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=smoke
```

### Default development accounts (from `database/seed.sql`)

| Role                | Name              | Email                     | Password     |
|---------------------|-------------------|---------------------------|--------------|
| Admin               | Shivam Yadav      | apex.innovator.team@gmail.com | `Apex@2000` |
| Core member         | Lipsarani Bisoyi  | core@apexinnovators.dev   | `Core@123`   |
| Core member         | Aryan Gupta       | member@apexinnovators.dev | `Member@123` |

> The seeded accounts carry the team roster above with role-based bios; finish the
> personal details (photos, links, richer bios) and the hackathon program
> name/certificates through the admin dashboard, and change all passwords on first login.

## Feature map (implementation plan §24 MVP)

| Area | Included |
|------|----------|
| Public | Home, Projects + filters, Project Details, Hackathons + details, Team, Achievements, Contact |
| Backend | REST + JPA/Hibernate + MySQL, JWT access/refresh, role security, Swagger/OpenAPI |
| Admin | Dashboard, Projects CRUD + moderation, Hackathons CRUD, Users, Posts moderation, Achievements, Messages, Technologies, audit log |
| UX | Dark futuristic design system, responsive (desktop → mobile), loading/empty/error states everywhere, search + filtering |
| First content | IntelliERP case study, Build With 2.0 hackathon journey, technology catalog |

Project submission workflow: `DRAFT → PENDING_REVIEW → APPROVED/PUBLISHED` with admin
moderation and full audit trail for every admin action.

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — product vision, stack, backend/frontend structure, security model
- [`docs/api.md`](docs/api.md) — complete REST surface, DTO shapes, status semantics
- [`docs/database.md`](docs/database.md) — schema, relationships, seeding
- [`docs/deployment.md`](docs/deployment.md) — production topology, env vars, checklist
- [`backend/README.md`](backend/README.md) — running the API

## Verification notes

The backend is exercised end-to-end (public + auth + admin flows) through the `smoke`
profile — see `backend/src/main/java/com/apexinnovators/config/SmokeDataSeeder.java`.
The frontend renders live data from the API on every data-driven page.

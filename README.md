# [Apex Innovators](https://sky-ydv2008.github.io/Team.Apex/) — Student Developer Team Platform

[![Apex Innovators Website](https://img.shields.io/badge/Apex%20Innovators-Live%20Platform-22d3ee?style=for-the-badge&logo=react)](https://sky-ydv2008.github.io/Team.Apex/)
[![Render Live API](https://img.shields.io/badge/Render%20Live-Single%20Origin-a855f7?style=for-the-badge&logo=render)](https://apex-innovators.onrender.com/)

A living digital home for [**Apex Innovators**](https://sky-ydv2008.github.io/Team.Apex/) student developer team at Parul Institute of Engineering and Technology: a **portfolio + AI skill evaluation + hackathon archive + project showcase + community platform** where every project tells a story — idea → research → build → hackathon → result → learnings.

### 🌐 Official Platform Links
- **Official Website**: [https://sky-ydv2008.github.io/Team.Apex/](https://sky-ydv2008.github.io/Team.Apex/)
- **Live Render Web App & REST API**: [https://apex-innovators.onrender.com/](https://apex-innovators.onrender.com/)
- **Android App (APK)**: [Download ApexInnovators.apk](https://github.com/Sky-ydv2008/Team.Apex/releases/download/v1.0.0/ApexInnovators.apk)
- **Windows App (.exe)**: [Download ApexInnovators-Windows.exe](https://github.com/Sky-ydv2008/Team.Apex/releases/download/v1.0.3/ApexInnovators-Windows.exe)
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
| Admin               | Shivam Yadav      | apex.innovator.team@gmail.com | `Apex@Shivam` |
| Core member         | Lipsarani Bisoyi  | bisoyilipsarani@gmail.com  | `Apex@Lipsa` |
| Core member         | Aryan Gupta       | the.aryangupta10@gmail.com | `Apex@Aryan` |

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

# Database — Apex Innovators

MySQL 8, database `apex_innovators`, charset `utf8mb4`. The schema is **owned by
`database/schema.sql`**; JPA runs with `ddl-auto: none` in production so entities and DDL
never drift silently. Run order:

```bash
mysql -u root -p < database/schema.sql        # creates DB + tables
mysql -u root -p apex_innovators < database/seed.sql
```

## Tables

| Table | Purpose | Notes |
|-------|---------|-------|
| `users` | accounts | unique email, `role` ADMIN/CORE_MEMBER/MEMBER, `status` ACTIVE/SUSPENDED |
| `profiles` | member profile | 1:1 users, bio/headline/photo/github/linkedin |
| `projects` | showcase entries | unique `slug`, `status` DRAFT…PUBLISHED, `featured`, year, github/demo/docs links |
| `project_members` | team on a project | (project_id, user_id) unique, role + contribution |
| `technologies` | catalog | unique name, category + icon |
| `project_technologies` | many-to-many | composite PK (project_id, technology_id) |
| `project_images` | gallery | ordered screenshots (reserved for future use) |
| `hackathons` | archive | unique slug, date/result/certificate/presentation URLs |
| `hackathon_members` | participation | (hackathon_id, user_id) unique |
| `hackathon_projects` | hackathon ↔ project | composite PK |
| `achievements` | awards/certificates | type AWARD/FINALIST/CERTIFICATE/MENTION/OTHER |
| `posts` | community content | author + type + status state machine |
| `comments` | post discussion | author + post |
| `post_likes` | likes | composite PK (post_id, user_id) |
| `bookmarks` | saved content | post OR project (Phase 7) |
| `events` | events | online/offline/hybrid (Phase 7) |
| `event_registrations` | attendance | (event_id, user_id) unique (Phase 7) |
| `notifications` | per-user | read_at null = unread (Phase 7) |
| `contact_messages` | contact form | status NEW/READ/REPLIED |
| `audit_logs` | admin trail | actor/action/entity/entity_id/detail |

## Key relationships

```
User 1──N ProjectMember N──1 Project
User 1──N Post            Post 1──N Comment
Project N──N Technology   Project N──N Hackathon
User N──N Hackathon       User 1──N Achievement
User 1──N ContactMessage  User 1──N AuditLog (actor)
```

All FKs are `ON DELETE CASCADE` where ownership implies deletion; deletion of users is
not exposed by the API (accounts are suspended instead).

## Reserved-word columns

`projects.year`, `hackathons.date`, `events.date` are mapped with dialect-aware quoting
(`` `year` `` / `` `date ``) so Hibernate emits correct DDL/DML on both MySQL (backtick)
and H2 (double quote) — the smoke profile boots on H2 in `MODE=MySQL`.

## Seeding (`database/seed.sql`)

First content per the implementation plan:

- **IntelliERP** — the Build With 2.0 case study: role-based Mini ERP Core, Analytics
  Dashboard, AI Business Advisor with rule-based stockout forecasting, supplier-loss
  detection and automatic product classification; 12 technologies; 2 project members.
- **Build With 2.0 hackathon** — linked to IntelliERP with both members.
- **Technology catalog** — Java, Spring Boot, Spring Security, JWT, MySQL, Hibernate/JPA,
  Maven, JavaScript, HTML5, CSS3, REST APIs, Swagger/OpenAPI, Git/GitHub.
- **Demo role accounts** (BCrypt-hashed) — see README table. These are placeholders for
  development; replace with real team profiles before launch.

## Smoke profile (no MySQL required)

`application-smoke.yml` boots the API against in-memory H2 (`MODE=MySQL`) with
`ddl-auto=create-drop`, and `SmokeDataSeeder` mirrors the seed above programmatically —
same accounts, project, hackathon, post and message. Start it with:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=smoke
```

H2 console: `/h2-console` (JDBC URL `jdbc:h2:mem:apex`, user `sa`, empty password).

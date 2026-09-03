-- =====================================================================
-- Apex Innovators — seed data (run AFTER database/schema.sql)
--   mysql -u <user> -p apex_innovators < database/seed.sql
--
-- DEMO CONTENT NOTICE
--   * Accounts below are development/demo identities used to exercise
--     ADMIN / CORE_MEMBER / MEMBER roles. Replace them with real team
--     profiles (and the real hackathon program name/organizer) via the
--     admin dashboard before launch — this repository must not ship
--     fabricated personal data.
--   * Passwords: Apex@2000 / Core@123 / Member@123 (BCrypt hashes).
--     Change them after first login.
-- =====================================================================

USE apex_innovators;

-- ---------------------------------------------------------------------
-- Users (demo role accounts)
-- ---------------------------------------------------------------------
INSERT INTO users (id, name, email, password_hash, role, status) VALUES
  (1, 'Apex Admin',    'apex.innovator.team@gmail.com',  '$2b$10$kRInQScjDcnnt4n/h3pMH.U76AbnptjfL0rVAQslXMyJZ9qLpy6pO', 'ADMIN',       'ACTIVE'),
  (2, 'Core Builder',  'core@apexinnovators.dev',   '$2b$10$S1BNGvaRonwxarbN66zIjuX9g.YkFNppQ0MRUZenaGFdSJQ3d3DTm', 'CORE_MEMBER', 'ACTIVE'),
  (3, 'Demo Member',   'member@apexinnovators.dev', '$2b$10$C1ShMEbgPxOHFAZ9IngGi.M03rDORRKxgAk7q7khRJoi.wfJFbQgW', 'MEMBER',      'ACTIVE');

INSERT INTO profiles (user_id, bio, headline, github, linkedin) VALUES
  (1, 'Administrator of the Apex Innovators platform. Replace this demo profile with a real team member via the admin dashboard.',
   'Platform Administrator', NULL, NULL),
  (2, 'Core builder on the Apex Innovators team — hackathon developer. Replace this demo profile with a real team member via the admin dashboard.',
   'Core Team · Developer', NULL, NULL),
  (3, 'Demo community member account used for role testing.', 'Community Member', NULL, NULL);

-- ---------------------------------------------------------------------
-- Technology catalog
-- ---------------------------------------------------------------------
INSERT INTO technologies (id, name, category, icon) VALUES
  (1,  'Java',           'Language',   'devicon-java'),
  (2,  'JavaScript',     'Language',   'devicon-javascript'),
  (3,  'HTML5',          'Frontend',   'devicon-html5'),
  (4,  'CSS3',           'Frontend',   'devicon-css3'),
  (5,  'Spring Boot',    'Framework',  'devicon-spring'),
  (6,  'Spring Security','Security',   'devicon-spring'),
  (7,  'JWT',            'Security',   'shield'),
  (8,  'MySQL',          'Database',   'devicon-mysql'),
  (9,  'Maven',          'Tooling',    'devicon-maven'),
  (10, 'REST APIs',      'Integration','server'),
  (11, 'Swagger / OpenAPI', 'Docs',    'file-code'),
  (12, 'Hibernate / JPA','Database',   'database'),
  (13, 'Git / GitHub',   'Tooling',    'devicon-github');

-- ---------------------------------------------------------------------
-- First showcase project: IntelliERP (implementation plan §18)
-- ---------------------------------------------------------------------
INSERT INTO projects
  (id, title, slug, tagline, description, problem, solution, status, featured,
   github_url, demo_url, docs_url, year)
VALUES
  (1, 'IntelliERP', 'intellierp',
   'AI-powered ERP & Business Intelligence for small businesses',
   'IntelliERP is a self-contained ERP and business-intelligence platform that pairs a Mini ERP Core with an Analytics Dashboard and an AI Business Advisor. Built for the Build With 2.0 hackathon, it gives small businesses a single pane of glass over products, inventory, purchases, sales, expenses and employees — with rule-based predictive intelligence that runs without any external AI dependency.',
   'Small businesses operate on spreadsheets and gut feeling. They lack actionable visibility into inventory risk, supplier reliability and overall business health, and affordable ERP tools are built for enterprises, not for them.',
   'IntelliERP combines a role-based Mini ERP Core (products, inventory, purchases, sales, expenses, employees) with an Analytics Dashboard and an AI Business Advisor. Predictive logic — stockout forecasting from average daily sales, current stock and supplier lead time, supplier-loss detection, and automatic product classification — runs as transparent rule-based algorithms over transactional data, keeping the hackathon build fully self-contained.',
   'PUBLISHED', 1,
   'https://github.com/Sky-ydv2008/ai-powered-mini-erp', NULL, NULL, 2026);

INSERT INTO project_members (id, project_id, user_id, role, contribution) VALUES
  (1, 1, 1, 'Product & Platform Lead', 'ERP module design, admin experience, integration'),
  (2, 1, 2, 'Backend Developer',       'Spring Boot REST APIs, Spring Security + JWT, predictive inventory rules');

INSERT INTO project_technologies (project_id, technology_id) VALUES
  (1, 1), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 2), (1, 3), (1, 4), (1, 10), (1, 11), (1, 12);

-- ---------------------------------------------------------------------
-- Hackathon archive: Build With 2.0 (program name/organizer: update from
-- the team''s certificate once verified)
-- ---------------------------------------------------------------------
INSERT INTO hackathons
  (id, name, slug, organizer, date, description, challenge, result,
   certificate_url, presentation_url)
VALUES
  (1, 'Build With 2.0', 'build-with-2.0', NULL, NULL,
   'Hackathon journey of Apex Innovators for Build With 2.0, where the team built IntelliERP — an AI-powered ERP & Business Intelligence solution.',
   'AI-Powered ERP & Business Intelligence: build a product that gives small businesses real, actionable intelligence.',
   'Team member for Build With 2.0 — result and certificate details to be added from the official announcement.',
   NULL, NULL);

INSERT INTO hackathon_members (id, hackathon_id, user_id) VALUES
  (1, 1, 1),
  (2, 1, 2);

INSERT INTO hackathon_projects (hackathon_id, project_id) VALUES (1, 1);

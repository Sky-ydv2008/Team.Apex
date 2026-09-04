-- =====================================================================
-- Apex Innovators — seed data (run AFTER database/schema.sql)
--   mysql -u <user> -p apex_innovators < database/seed.sql
--
-- DEMO CONTENT NOTICE
--   * Accounts below seed the real team roster — update personal bios, links
--     and the hackathon program name/organizer via the admin dashboard.
--   * Passwords: Apex@Shivam / Apex@Lipsa / Apex@Aryan (BCrypt hashes).
--     Change them after first login.
-- =====================================================================

USE apex_innovators;

-- ---------------------------------------------------------------------
-- Users (demo role accounts)
-- ---------------------------------------------------------------------
INSERT INTO users (id, name, email, password_hash, role, status) VALUES
  (1, 'Shivam Yadav',   'apex.innovator.team@gmail.com',  '$2b$10$.0mtgp6heB2Kyxv0dbY.6eEHJbtALW5/raKj4t4ZXPTXA6wmUp.pe', 'ADMIN',       'ACTIVE'),
  (2, 'Lipsarani Bisoyi','bisoyilipsarani@gmail.com',   '$2b$10$BZkIaMuzxkXyULZOQDlPRuObtjiAU4XzZ/V.LsBo6gJPcDYchiSva', 'CORE_MEMBER', 'ACTIVE'),
  (3, 'Aryan Gupta',    'the.aryangupta10@gmail.com', '$2b$10$dtGBA0xcTTi3CxwaZej2.uWxFu23IsB8ueM3H8EKTDmYEmZisCIv2', 'CORE_MEMBER', 'ACTIVE');

INSERT INTO profiles (user_id, bio, headline, github, linkedin) VALUES
  (1, 'Java backend developer on Apex Innovators — Spring Boot, REST APIs, JWT security and the platform core.',
   'Java Backend Developer', NULL, NULL),
  (2, 'Frontend developer on Apex Innovators — crafting the interfaces, design system and user experience.',
   'Frontend Developer', NULL, NULL),
  (3, 'Full stack developer on Apex Innovators — backend to browser, from database to deployed product.',
   'Full Stack Developer', NULL, NULL);

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
   'https://github.com/Sky-ydv2008/ai-powered-mini-erp', 'https://sky-ydv2008.github.io/Team.Apex/', NULL, 2026);

INSERT INTO project_members (id, project_id, user_id, role, contribution) VALUES
  (1, 1, 1, 'Java Backend Developer',   'Spring Boot REST APIs, Spring Security + JWT, predictive inventory logic'),
  (2, 1, 2, 'Frontend Developer',        'UI, design system and client-side integration'),
  (3, 1, 3, 'Full Stack Developer',      'Full stack integration, decision engine testing & cloud deployment');

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
  (2, 1, 2),
  (3, 1, 3);

INSERT INTO hackathon_projects (hackathon_id, project_id) VALUES (1, 1);

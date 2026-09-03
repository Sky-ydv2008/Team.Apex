-- =====================================================================
-- Apex Innovators — MySQL schema (v1, MVP per implementation plan §11–12)
-- Engine: MySQL 8.0+  |  Charset: utf8mb4
-- Apply:  mysql -u <user> -p apex_innovators < database/schema.sql
-- Seed:   database/seed.sql (run after this file)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS apex_innovators
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE apex_innovators;

-- ---------------------------------------------------------------------
-- Accounts & profiles
-- ---------------------------------------------------------------------

CREATE TABLE users (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(120)    NOT NULL,
  email         VARCHAR(190)    NOT NULL,
  password_hash VARCHAR(100)    NOT NULL,
  role          ENUM('ADMIN','CORE_MEMBER','MEMBER') NOT NULL DEFAULT 'MEMBER',
  status        ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE profiles (
  id        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id   BIGINT UNSIGNED NOT NULL,
  bio       TEXT,
  photo_url VARCHAR(500),
  github    VARCHAR(190),
  linkedin  VARCHAR(190),
  headline  VARCHAR(190),
  PRIMARY KEY (id),
  UNIQUE KEY uq_profiles_user (user_id),
  CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Project showcase
-- ---------------------------------------------------------------------

CREATE TABLE projects (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title           VARCHAR(190)    NOT NULL,
  slug            VARCHAR(210)    NOT NULL,
  tagline         VARCHAR(300),
  description     TEXT,
  problem         TEXT,
  solution        TEXT,
  status          ENUM('DRAFT','PENDING_REVIEW','APPROVED','REJECTED','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
  featured        TINYINT(1)      NOT NULL DEFAULT 0,
  github_url      VARCHAR(500),
  demo_url        VARCHAR(500),
  docs_url        VARCHAR(500),
  year            SMALLINT UNSIGNED,
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_projects_slug (slug),
  KEY idx_projects_status (status),
  KEY idx_projects_featured (featured)
) ENGINE=InnoDB;

CREATE TABLE project_members (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  project_id   BIGINT UNSIGNED NOT NULL,
  user_id      BIGINT UNSIGNED NOT NULL,
  role         VARCHAR(120),
  contribution VARCHAR(500),
  PRIMARY KEY (id),
  UNIQUE KEY uq_project_members (project_id, user_id),
  CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
  CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE technologies (
  id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name     VARCHAR(120)    NOT NULL,
  category VARCHAR(120),
  icon     VARCHAR(500),
  PRIMARY KEY (id),
  UNIQUE KEY uq_technologies_name (name)
) ENGINE=InnoDB;

CREATE TABLE project_technologies (
  project_id    BIGINT UNSIGNED NOT NULL,
  technology_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (project_id, technology_id),
  CONSTRAINT fk_pt_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
  CONSTRAINT fk_pt_technology FOREIGN KEY (technology_id) REFERENCES technologies (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE project_images (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  project_id BIGINT UNSIGNED NOT NULL,
  image_url  VARCHAR(500)    NOT NULL,
  caption    VARCHAR(300),
  sort_order INT             NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_pi_project (project_id),
  CONSTRAINT fk_pi_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Hackathon archive
-- ---------------------------------------------------------------------

CREATE TABLE hackathons (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name         VARCHAR(190)    NOT NULL,
  slug         VARCHAR(210)    NOT NULL,
  organizer    VARCHAR(190),
  date         DATE,
  description  TEXT,
  challenge    TEXT,
  result       VARCHAR(300),
  certificate_url VARCHAR(500),
  presentation_url VARCHAR(500),
  created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_hackathons_slug (slug)
) ENGINE=InnoDB;

CREATE TABLE hackathon_members (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  hackathon_id BIGINT UNSIGNED NOT NULL,
  user_id      BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_hackathon_members (hackathon_id, user_id),
  CONSTRAINT fk_hm_hackathon FOREIGN KEY (hackathon_id) REFERENCES hackathons (id) ON DELETE CASCADE,
  CONSTRAINT fk_hm_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Hackathon <-> project links (a hackathon result can link a project)
CREATE TABLE hackathon_projects (
  hackathon_id BIGINT UNSIGNED NOT NULL,
  project_id   BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (hackathon_id, project_id),
  CONSTRAINT fk_hp_hackathon FOREIGN KEY (hackathon_id) REFERENCES hackathons (id) ON DELETE CASCADE,
  CONSTRAINT fk_hp_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Achievements (§5.6)
-- ---------------------------------------------------------------------

CREATE TABLE achievements (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  title       VARCHAR(190)    NOT NULL,
  type        ENUM('AWARD','FINALIST','CERTIFICATE','MENTION','OTHER') NOT NULL DEFAULT 'CERTIFICATE',
  issuer      VARCHAR(190),
  award_date  DATE,
  description TEXT,
  verify_url  VARCHAR(500),
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_achievements_user (user_id),
  CONSTRAINT fk_achievements_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Community
-- ---------------------------------------------------------------------

CREATE TABLE posts (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  author_id  BIGINT UNSIGNED NOT NULL,
  type       ENUM('DISCUSSION','PROJECT','ACHIEVEMENT','HACKATHON','RESOURCE','QUESTION','ANNOUNCEMENT') NOT NULL DEFAULT 'DISCUSSION',
  title      VARCHAR(190)    NOT NULL,
  body       TEXT,
  status     ENUM('DRAFT','PENDING_REVIEW','APPROVED','REJECTED','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_posts_status (status),
  KEY idx_posts_author (author_id),
  CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE comments (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id    BIGINT UNSIGNED NOT NULL,
  author_id  BIGINT UNSIGNED NOT NULL,
  body       TEXT            NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_comments_post (post_id),
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE post_likes (
  post_id    BIGINT UNSIGNED NOT NULL,
  user_id    BIGINT UNSIGNED NOT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (post_id, user_id),
  CONSTRAINT fk_pl_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  CONSTRAINT fk_pl_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE bookmarks (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  post_id    BIGINT UNSIGNED,
  project_id BIGINT UNSIGNED,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_bookmarks_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
  CONSTRAINT fk_bookmarks_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Events & notifications (forward-looking, Phase 7)
-- ---------------------------------------------------------------------

CREATE TABLE events (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title       VARCHAR(190)    NOT NULL,
  date        DATETIME,
  mode        ENUM('ONLINE','OFFLINE','HYBRID') NOT NULL DEFAULT 'ONLINE',
  location    VARCHAR(300),
  description TEXT,
  created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE event_registrations (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_id   BIGINT UNSIGNED NOT NULL,
  user_id    BIGINT UNSIGNED NOT NULL,
  status     ENUM('REGISTERED','ATTENDED','CANCELLED') NOT NULL DEFAULT 'REGISTERED',
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_event_registrations (event_id, user_id),
  CONSTRAINT fk_er_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
  CONSTRAINT fk_er_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id    BIGINT UNSIGNED NOT NULL,
  type       VARCHAR(60),
  message    VARCHAR(500),
  read_at    TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notifications_user (user_id),
  CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Contact & audit
-- ---------------------------------------------------------------------

CREATE TABLE contact_messages (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name       VARCHAR(120)    NOT NULL,
  email      VARCHAR(190)    NOT NULL,
  subject    VARCHAR(190),
  message    TEXT            NOT NULL,
  status     ENUM('NEW','READ','REPLIED') NOT NULL DEFAULT 'NEW',
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_cm_status (status)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_id   BIGINT UNSIGNED,
  action     VARCHAR(120)    NOT NULL,
  entity     VARCHAR(120),
  entity_id  BIGINT UNSIGNED,
  detail     TEXT,
  created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_created (created_at),
  KEY idx_audit_actor (actor_id)
) ENGINE=InnoDB;

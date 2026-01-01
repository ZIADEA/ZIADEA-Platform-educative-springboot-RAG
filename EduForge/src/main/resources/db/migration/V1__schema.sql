-- EduForge V1 Schema Initial (PostgreSQL)

-- Users
CREATE TABLE IF NOT EXISTS app_user (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(40) NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_email ON app_user(email);

-- Institution
CREATE TABLE IF NOT EXISTS institution (
  id BIGSERIAL PRIMARY KEY,
  owner_user_id BIGINT NOT NULL UNIQUE,
  name VARCHAR(180) NOT NULL,
  type VARCHAR(30) NOT NULL,
  country VARCHAR(80) NOT NULL,
  city VARCHAR(80) NOT NULL,
  address VARCHAR(180),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_inst_owner FOREIGN KEY (owner_user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Institution Membership
CREATE TABLE IF NOT EXISTS institution_membership (
  id BIGSERIAL PRIMARY KEY,
  institution_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_mem_inst FOREIGN KEY (institution_id) REFERENCES institution(id) ON DELETE CASCADE,
  CONSTRAINT fk_mem_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT uq_inst_user UNIQUE (institution_id, user_id)
);

-- Program
CREATE TABLE IF NOT EXISTS program (
  id BIGSERIAL PRIMARY KEY,
  institution_id BIGINT NOT NULL,
  name VARCHAR(160) NOT NULL,
  CONSTRAINT fk_prog_inst FOREIGN KEY (institution_id) REFERENCES institution(id) ON DELETE CASCADE
);

-- Academic Level
CREATE TABLE IF NOT EXISTS academic_level (
  id BIGSERIAL PRIMARY KEY,
  program_id BIGINT NOT NULL,
  label VARCHAR(80) NOT NULL,
  CONSTRAINT fk_level_prog FOREIGN KEY (program_id) REFERENCES program(id) ON DELETE CASCADE
);

-- Subject
CREATE TABLE IF NOT EXISTS subject (
  id BIGSERIAL PRIMARY KEY,
  level_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  CONSTRAINT fk_sub_level FOREIGN KEY (level_id) REFERENCES academic_level(id) ON DELETE CASCADE
);

-- Classroom
CREATE TABLE IF NOT EXISTS classroom (
  id BIGSERIAL PRIMARY KEY,
  owner_prof_id BIGINT NOT NULL,
  institution_id BIGINT,
  title VARCHAR(160) NOT NULL,
  code VARCHAR(16) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_class_prof FOREIGN KEY (owner_prof_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_class_inst FOREIGN KEY (institution_id) REFERENCES institution(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_classroom_code ON classroom(code);

-- Classroom Enrollment
CREATE TABLE IF NOT EXISTS classroom_enrollment (
  id BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_enr_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
  CONSTRAINT fk_enr_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT uq_class_student UNIQUE (classroom_id, student_id)
);

-- Classroom Post
CREATE TABLE IF NOT EXISTS classroom_post (
  id BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_post_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
  CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES app_user(id) ON DELETE CASCADE
);
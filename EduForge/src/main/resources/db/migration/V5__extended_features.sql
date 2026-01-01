-- EduForge V5 - Extended Features Schema
-- Bibliothèque, Examens, Niveaux de classe, Messagerie, Échelles de notation

-- 1. Extension de l'institution : échelle de notation et paramètres
ALTER TABLE institution ADD COLUMN IF NOT EXISTS grading_scale VARCHAR(20) DEFAULT 'SCALE_20';
ALTER TABLE institution ADD COLUMN IF NOT EXISTS grading_method VARCHAR(20) DEFAULT 'STANDARD';
ALTER TABLE institution ADD COLUMN IF NOT EXISTS default_pass_threshold INT DEFAULT 50;
ALTER TABLE institution ADD COLUMN IF NOT EXISTS negative_marking BOOLEAN DEFAULT FALSE;
ALTER TABLE institution ADD COLUMN IF NOT EXISTS correct_points DECIMAL(4,2) DEFAULT 1.00;
ALTER TABLE institution ADD COLUMN IF NOT EXISTS wrong_points DECIMAL(4,2) DEFAULT 0.00;
ALTER TABLE institution ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE institution ADD COLUMN IF NOT EXISTS logo_path VARCHAR(500);
ALTER TABLE institution ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE institution ADD COLUMN IF NOT EXISTS phone VARCHAR(30);

-- 2. Extension utilisateur : affiliation institution + métadonnées
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS institution_id BIGINT;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS program_id BIGINT;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS level_id BIGINT;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS affiliation_status VARCHAR(30) DEFAULT 'INDEPENDENT';
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS avatar_path VARCHAR(500);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS phone VARCHAR(30);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS last_login TIMESTAMPTZ;

-- Clés étrangères pour les affiliations
ALTER TABLE app_user 
  ADD CONSTRAINT fk_user_institution 
  FOREIGN KEY (institution_id) REFERENCES institution(id) ON DELETE SET NULL;

ALTER TABLE app_user 
  ADD CONSTRAINT fk_user_program 
  FOREIGN KEY (program_id) REFERENCES program(id) ON DELETE SET NULL;

ALTER TABLE app_user 
  ADD CONSTRAINT fk_user_level 
  FOREIGN KEY (level_id) REFERENCES academic_level(id) ON DELETE SET NULL;

-- 3. Extension classroom : niveaux et paramètres
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS subject_id BIGINT;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS level_id BIGINT;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT FALSE;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS max_students INT DEFAULT 0;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS grading_scale VARCHAR(20);
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS pass_threshold INT DEFAULT 50;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS allow_retakes BOOLEAN DEFAULT TRUE;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS max_retakes INT DEFAULT 3;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS negative_marking BOOLEAN DEFAULT FALSE;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS correct_points DECIMAL(4,2) DEFAULT 1.00;
ALTER TABLE classroom ADD COLUMN IF NOT EXISTS wrong_points DECIMAL(4,2) DEFAULT 0.00;

ALTER TABLE classroom 
  ADD CONSTRAINT fk_class_subject 
  FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE SET NULL;

ALTER TABLE classroom 
  ADD CONSTRAINT fk_class_level 
  FOREIGN KEY (level_id) REFERENCES academic_level(id) ON DELETE SET NULL;

-- 4. Système de niveaux dans une classe (progression)
CREATE TABLE IF NOT EXISTS classroom_level (
  id BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL,
  level_number INT NOT NULL DEFAULT 1,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  required_score INT DEFAULT 70,
  unlock_threshold INT DEFAULT 80,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_cl_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
  CONSTRAINT uq_class_level_num UNIQUE (classroom_id, level_number)
);

-- 5. Progression étudiant dans les niveaux de classe
CREATE TABLE IF NOT EXISTS student_progress (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  classroom_id BIGINT NOT NULL,
  current_level_id BIGINT,
  total_score INT DEFAULT 0,
  total_attempts INT DEFAULT 0,
  courses_completed INT DEFAULT 0,
  quizzes_passed INT DEFAULT 0,
  average_score DECIMAL(5,2) DEFAULT 0.00,
  status VARCHAR(30) DEFAULT 'IN_PROGRESS',
  started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_activity TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  CONSTRAINT fk_sp_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp_classroom FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
  CONSTRAINT fk_sp_level FOREIGN KEY (current_level_id) REFERENCES classroom_level(id) ON DELETE SET NULL,
  CONSTRAINT uq_student_class_progress UNIQUE (student_id, classroom_id)
);

-- 6. Bibliothèque institutionnelle
CREATE TABLE IF NOT EXISTS library_resource (
  id BIGSERIAL PRIMARY KEY,
  institution_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(80),
  file_type VARCHAR(20) NOT NULL DEFAULT 'PDF',
  original_name VARCHAR(255) NOT NULL,
  stored_path VARCHAR(500) NOT NULL,
  file_size BIGINT DEFAULT 0,
  author VARCHAR(180),
  isbn VARCHAR(30),
  published_year INT,
  is_public BOOLEAN DEFAULT FALSE,
  download_count INT DEFAULT 0,
  uploaded_by BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_lib_inst FOREIGN KEY (institution_id) REFERENCES institution(id) ON DELETE CASCADE,
  CONSTRAINT fk_lib_uploader FOREIGN KEY (uploaded_by) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_lib_inst ON library_resource(institution_id);
CREATE INDEX IF NOT EXISTS idx_lib_category ON library_resource(category);

-- 7. Système d'examens programmés
CREATE TABLE IF NOT EXISTS exam (
  id BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT NOT NULL,
  course_id BIGINT,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  exam_type VARCHAR(30) NOT NULL DEFAULT 'QUIZ',
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
  question_count INT NOT NULL DEFAULT 10,
  duration_minutes INT DEFAULT 60,
  pass_threshold INT DEFAULT 50,
  scheduled_start TIMESTAMPTZ,
  scheduled_end TIMESTAMPTZ,
  is_timed BOOLEAN DEFAULT TRUE,
  is_scheduled BOOLEAN DEFAULT FALSE,
  allow_review BOOLEAN DEFAULT TRUE,
  shuffle_questions BOOLEAN DEFAULT TRUE,
  shuffle_answers BOOLEAN DEFAULT TRUE,
  show_score_immediately BOOLEAN DEFAULT TRUE,
  max_attempts INT DEFAULT 1,
  created_by BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  published_at TIMESTAMPTZ,
  CONSTRAINT fk_exam_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE,
  CONSTRAINT fk_exam_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE SET NULL,
  CONSTRAINT fk_exam_creator FOREIGN KEY (created_by) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_exam_class ON exam(classroom_id);
CREATE INDEX IF NOT EXISTS idx_exam_status ON exam(status);
CREATE INDEX IF NOT EXISTS idx_exam_schedule ON exam(scheduled_start, scheduled_end);

-- 8. Questions d'examen (copiées depuis quiz ou créées manuellement)
CREATE TABLE IF NOT EXISTS exam_question (
  id BIGSERIAL PRIMARY KEY,
  exam_id BIGINT NOT NULL,
  q_index INT NOT NULL,
  question_text TEXT NOT NULL,
  a_text TEXT NOT NULL,
  b_text TEXT NOT NULL,
  c_text TEXT NOT NULL,
  d_text TEXT NOT NULL,
  correct_choice VARCHAR(1) NOT NULL,
  explanation TEXT,
  points DECIMAL(4,2) DEFAULT 1.00,
  CONSTRAINT fk_eq_exam FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_exam ON exam_question(exam_id);

-- 9. Tentatives d'examen
CREATE TABLE IF NOT EXISTS exam_attempt (
  id BIGSERIAL PRIMARY KEY,
  exam_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  submitted_at TIMESTAMPTZ,
  score DECIMAL(5,2),
  score_percent INT,
  correct_count INT DEFAULT 0,
  total_count INT DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
  time_spent_seconds INT DEFAULT 0,
  attempt_number INT DEFAULT 1,
  CONSTRAINT fk_ea_exam FOREIGN KEY (exam_id) REFERENCES exam(id) ON DELETE CASCADE,
  CONSTRAINT fk_ea_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ea_exam ON exam_attempt(exam_id);
CREATE INDEX IF NOT EXISTS idx_ea_student ON exam_attempt(student_id);
CREATE INDEX IF NOT EXISTS idx_ea_status ON exam_attempt(status);

-- 10. Réponses d'examen
CREATE TABLE IF NOT EXISTS exam_answer (
  id BIGSERIAL PRIMARY KEY,
  attempt_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  chosen_choice VARCHAR(1),
  is_correct BOOLEAN DEFAULT FALSE,
  answered_at TIMESTAMPTZ,
  CONSTRAINT fk_exa_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempt(id) ON DELETE CASCADE,
  CONSTRAINT fk_exa_question FOREIGN KEY (question_id) REFERENCES exam_question(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_exa_attempt ON exam_answer(attempt_id);

-- 11. Messagerie
CREATE TABLE IF NOT EXISTS message (
  id BIGSERIAL PRIMARY KEY,
  classroom_id BIGINT,
  sender_id BIGINT NOT NULL,
  recipient_id BIGINT,
  parent_id BIGINT,
  subject VARCHAR(255),
  content TEXT NOT NULL,
  message_type VARCHAR(30) DEFAULT 'DIRECT',
  is_read BOOLEAN DEFAULT FALSE,
  is_archived BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  read_at TIMESTAMPTZ,
  CONSTRAINT fk_msg_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE SET NULL,
  CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_recipient FOREIGN KEY (recipient_id) REFERENCES app_user(id) ON DELETE SET NULL,
  CONSTRAINT fk_msg_parent FOREIGN KEY (parent_id) REFERENCES message(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_msg_class ON message(classroom_id);
CREATE INDEX IF NOT EXISTS idx_msg_sender ON message(sender_id);
CREATE INDEX IF NOT EXISTS idx_msg_recipient ON message(recipient_id);
CREATE INDEX IF NOT EXISTS idx_msg_unread ON message(recipient_id, is_read) WHERE is_read = FALSE;

-- 12. Notifications
CREATE TABLE IF NOT EXISTS notification (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  link VARCHAR(500),
  notification_type VARCHAR(30) DEFAULT 'INFO',
  is_read BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  read_at TIMESTAMPTZ,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notif_user ON notification(user_id);
CREATE INDEX IF NOT EXISTS idx_notif_unread ON notification(user_id, is_read) WHERE is_read = FALSE;

-- 13. Cours publics (catalogue)
ALTER TABLE course ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT FALSE;
ALTER TABLE course ADD COLUMN IF NOT EXISTS category VARCHAR(80);
ALTER TABLE course ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20) DEFAULT 'INTERMEDIATE';
ALTER TABLE course ADD COLUMN IF NOT EXISTS estimated_hours INT DEFAULT 1;
ALTER TABLE course ADD COLUMN IF NOT EXISTS thumbnail_path VARCHAR(500);
ALTER TABLE course ADD COLUMN IF NOT EXISTS view_count INT DEFAULT 0;
ALTER TABLE course ADD COLUMN IF NOT EXISTS enrollment_count INT DEFAULT 0;

-- 14. Inscriptions aux cours publics
CREATE TABLE IF NOT EXISTS course_enrollment (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  progress_percent INT DEFAULT 0,
  completed BOOLEAN DEFAULT FALSE,
  enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMPTZ,
  last_accessed TIMESTAMPTZ,
  CONSTRAINT fk_ce_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT fk_ce_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT uq_course_student UNIQUE (course_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_ce_course ON course_enrollment(course_id);
CREATE INDEX IF NOT EXISTS idx_ce_student ON course_enrollment(student_id);

-- 15. Chapitres de cours
CREATE TABLE IF NOT EXISTS course_chapter (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  chapter_index INT NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  content TEXT,
  estimated_minutes INT DEFAULT 30,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_chap_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT uq_course_chapter UNIQUE (course_id, chapter_index)
);

CREATE INDEX IF NOT EXISTS idx_chap_course ON course_chapter(course_id);

-- 16. Association matériel-chapitre
ALTER TABLE course_material ADD COLUMN IF NOT EXISTS chapter_id BIGINT;
ALTER TABLE course_material 
  ADD CONSTRAINT fk_mat_chapter 
  FOREIGN KEY (chapter_id) REFERENCES course_chapter(id) ON DELETE SET NULL;

-- 17. Progression dans les chapitres
CREATE TABLE IF NOT EXISTS chapter_progress (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  chapter_id BIGINT NOT NULL,
  completed BOOLEAN DEFAULT FALSE,
  time_spent_seconds INT DEFAULT 0,
  started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMPTZ,
  CONSTRAINT fk_cp_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_cp_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapter(id) ON DELETE CASCADE,
  CONSTRAINT uq_student_chapter UNIQUE (student_id, chapter_id)
);

-- 18. Certificats
CREATE TABLE IF NOT EXISTS certificate (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL,
  course_id BIGINT,
  classroom_id BIGINT,
  certificate_type VARCHAR(30) NOT NULL DEFAULT 'COMPLETION',
  title VARCHAR(200) NOT NULL,
  description TEXT,
  issued_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  certificate_code VARCHAR(50) UNIQUE,
  final_score DECIMAL(5,2),
  pdf_path VARCHAR(500),
  CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_cert_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE SET NULL,
  CONSTRAINT fk_cert_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_cert_student ON certificate(student_id);
CREATE INDEX IF NOT EXISTS idx_cert_code ON certificate(certificate_code);

-- 19. Historique d'activité
CREATE TABLE IF NOT EXISTS activity_log (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  action_type VARCHAR(50) NOT NULL,
  entity_type VARCHAR(50),
  entity_id BIGINT,
  description TEXT,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_log_user ON activity_log(user_id);
CREATE INDEX IF NOT EXISTS idx_log_date ON activity_log(created_at);
CREATE INDEX IF NOT EXISTS idx_log_type ON activity_log(action_type);

-- 20. Tags pour cours
CREATE TABLE IF NOT EXISTS course_tag (
  id BIGSERIAL PRIMARY KEY,
  course_id BIGINT NOT NULL,
  tag VARCHAR(50) NOT NULL,
  CONSTRAINT fk_tag_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT uq_course_tag UNIQUE (course_id, tag)
);

CREATE INDEX IF NOT EXISTS idx_tag_name ON course_tag(tag);

-- 21. Favoris
CREATE TABLE IF NOT EXISTS favorite (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  course_id BIGINT,
  classroom_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_fav_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
  CONSTRAINT fk_fav_class FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_fav_user ON favorite(user_id);

-- 22. Extension quiz pour chapitres
ALTER TABLE quiz ADD COLUMN IF NOT EXISTS chapter_id BIGINT;
ALTER TABLE quiz ADD COLUMN IF NOT EXISTS classroom_id BIGINT;
ALTER TABLE quiz ADD COLUMN IF NOT EXISTS is_practice BOOLEAN DEFAULT TRUE;
ALTER TABLE quiz 
  ADD CONSTRAINT fk_quiz_chapter 
  FOREIGN KEY (chapter_id) REFERENCES course_chapter(id) ON DELETE SET NULL;
ALTER TABLE quiz 
  ADD CONSTRAINT fk_quiz_classroom 
  FOREIGN KEY (classroom_id) REFERENCES classroom(id) ON DELETE SET NULL;

-- Index supplémentaires pour les performances
CREATE INDEX IF NOT EXISTS idx_user_inst ON app_user(institution_id);
CREATE INDEX IF NOT EXISTS idx_user_role ON app_user(role);
CREATE INDEX IF NOT EXISTS idx_course_public ON course(is_public) WHERE is_public = TRUE;
CREATE INDEX IF NOT EXISTS idx_course_category ON course(category);

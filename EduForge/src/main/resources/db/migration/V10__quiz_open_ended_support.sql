-- V10__quiz_open_ended_support.sql
-- Ajout du support pour les questions ouvertes dans les quiz

ALTER TABLE quiz_question
ADD COLUMN IF NOT EXISTS question_type VARCHAR(20) DEFAULT 'MCQ',
ADD COLUMN IF NOT EXISTS expected_answer TEXT,
ADD COLUMN IF NOT EXISTS grading_criteria TEXT,
ADD COLUMN IF NOT EXISTS max_points INTEGER DEFAULT 10;

-- Rendre les colonnes QCM nullable pour supporter les questions ouvertes
ALTER TABLE quiz_question 
ALTER COLUMN a_text DROP NOT NULL,
ALTER COLUMN b_text DROP NOT NULL,
ALTER COLUMN c_text DROP NOT NULL,
ALTER COLUMN d_text DROP NOT NULL,
ALTER COLUMN correct_choice DROP NOT NULL;

-- Ajouter support pour stocker les réponses aux questions ouvertes
ALTER TABLE quiz_attempt_answer
ADD COLUMN IF NOT EXISTS text_answer TEXT,
ADD COLUMN IF NOT EXISTS ai_score DECIMAL(5,2),
ADD COLUMN IF NOT EXISTS ai_feedback TEXT,
ALTER COLUMN chosen_choice DROP NOT NULL;

-- Mettre à jour les questions existantes avec type MCQ
UPDATE quiz_question SET question_type = 'MCQ' WHERE question_type IS NULL;

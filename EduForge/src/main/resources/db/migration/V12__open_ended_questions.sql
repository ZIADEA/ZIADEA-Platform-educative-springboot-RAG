-- ============================================================
-- Migration V12: Support des questions à réponse ouverte
-- ============================================================

-- Ajouter le type de question dans exam_question
ALTER TABLE exam_question 
    ADD COLUMN IF NOT EXISTS question_type VARCHAR(20) DEFAULT 'MCQ';

-- Modifier les colonnes QCM pour qu'elles soient optionnelles
ALTER TABLE exam_question 
    ALTER COLUMN a_text DROP NOT NULL,
    ALTER COLUMN b_text DROP NOT NULL,
    ALTER COLUMN c_text DROP NOT NULL,
    ALTER COLUMN d_text DROP NOT NULL,
    ALTER COLUMN correct_choice DROP NOT NULL;

-- Ajouter les colonnes pour questions ouvertes
ALTER TABLE exam_question
    ADD COLUMN IF NOT EXISTS expected_answer TEXT,
    ADD COLUMN IF NOT EXISTS grading_rubric TEXT;

-- Ajouter le support des réponses textuelles dans exam_answer
ALTER TABLE exam_answer
    ADD COLUMN IF NOT EXISTS text_answer TEXT,
    ADD COLUMN IF NOT EXISTS ai_score INTEGER,
    ADD COLUMN IF NOT EXISTS ai_feedback TEXT;

-- Modifier chosen_choice pour qu'il soit optionnel
ALTER TABLE exam_answer
    ALTER COLUMN chosen_choice DROP NOT NULL;

-- Index pour les questions ouvertes
CREATE INDEX IF NOT EXISTS idx_eq_type ON exam_question(question_type);

COMMENT ON COLUMN exam_question.question_type IS 'MCQ ou OPEN_ENDED';
COMMENT ON COLUMN exam_question.expected_answer IS 'Réponse attendue pour question ouverte';
COMMENT ON COLUMN exam_question.grading_rubric IS 'Critères d''évaluation pour l''IA';
COMMENT ON COLUMN exam_answer.text_answer IS 'Réponse textuelle de l''étudiant';
COMMENT ON COLUMN exam_answer.ai_score IS 'Score donné par l''IA (0-100)';
COMMENT ON COLUMN exam_answer.ai_feedback IS 'Feedback pédagogique de l''IA';

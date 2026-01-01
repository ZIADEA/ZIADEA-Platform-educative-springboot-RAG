-- V11__reviewbook_table.sql
-- Table pour stocker les documents de révision personnelle des étudiants

CREATE TABLE IF NOT EXISTS review_book (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL DEFAULT 'TXT',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    extracted_text TEXT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    processed_at TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT check_file_type CHECK (file_type IN ('PDF', 'TXT', 'IMAGE')),
    CONSTRAINT check_status CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'FAILED'))
);

-- Index pour les recherches par étudiant
CREATE INDEX IF NOT EXISTS idx_review_book_student_id ON review_book(student_id);
CREATE INDEX IF NOT EXISTS idx_review_book_status ON review_book(status);

COMMENT ON TABLE review_book IS 'Documents personnels uploadés par les étudiants pour générer des quiz de révision';
COMMENT ON COLUMN review_book.extracted_text IS 'Texte extrait via PDFBox ou OCR Gemini';
COMMENT ON COLUMN review_book.file_type IS 'Type de fichier: PDF, TXT, ou IMAGE';

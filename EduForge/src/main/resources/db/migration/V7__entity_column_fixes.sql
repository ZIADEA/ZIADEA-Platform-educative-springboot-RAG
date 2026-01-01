-- V7: Fix entity-database column name mismatches

-- course_enrollment: rename columns to match entity
ALTER TABLE course_enrollment RENAME COLUMN completed TO is_completed;
ALTER TABLE course_enrollment RENAME COLUMN last_accessed TO last_accessed_at;
ALTER TABLE course_enrollment ADD COLUMN IF NOT EXISTS last_accessed_chapter_id BIGINT;

-- Create enrollment_completed_chapters table for ElementCollection
CREATE TABLE IF NOT EXISTS enrollment_completed_chapters (
  enrollment_id BIGINT NOT NULL REFERENCES course_enrollment(id) ON DELETE CASCADE,
  chapter_id BIGINT NOT NULL,
  PRIMARY KEY (enrollment_id, chapter_id)
);

-- EduForge V9 - Add target_classroom_id to courses for CLASSE publishing mode

-- Add target_classroom_id column to course table
ALTER TABLE course ADD COLUMN IF NOT EXISTS target_classroom_id BIGINT;

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_course_target_classroom ON course(target_classroom_id);

-- Add foreign key constraint (optional, depends on your model)
-- ALTER TABLE course ADD CONSTRAINT fk_course_target_classroom 
--     FOREIGN KEY (target_classroom_id) REFERENCES classroom(id) ON DELETE SET NULL;

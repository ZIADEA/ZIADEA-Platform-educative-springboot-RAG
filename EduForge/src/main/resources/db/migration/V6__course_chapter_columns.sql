-- V6: Add missing columns to course_chapter table for CourseChapterService support

-- Rename chapter_index to chapter_order
ALTER TABLE course_chapter RENAME COLUMN chapter_index TO chapter_order;

-- Add new columns for content management
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS content_type VARCHAR(20);
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS content_path VARCHAR(500);
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS text_content TEXT;
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS video_url VARCHAR(500);
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT FALSE;
ALTER TABLE course_chapter ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Rename estimated_minutes to duration_minutes
ALTER TABLE course_chapter RENAME COLUMN estimated_minutes TO duration_minutes;

-- Update existing constraint name if needed (the index already exists)
-- Rename the unique constraint
ALTER TABLE course_chapter DROP CONSTRAINT IF EXISTS uq_course_chapter;
ALTER TABLE course_chapter ADD CONSTRAINT uq_course_chapter UNIQUE (course_id, chapter_order);

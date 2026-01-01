-- EduForge V4 - Seed Data
-- Password for all: "password123" (plain text - NoOpPasswordEncoder)

-- 1. Insert test users (plain text passwords for development)
INSERT INTO app_user (full_name, email, password_hash, role, status, created_at) 
VALUES
  ('Admin System', 'admin@eduforge.local', 'password123', 'ADMIN', 'ACTIVE', NOW()),
  ('Professeur Test', 'prof@eduforge.local', 'password123', 'PROF', 'ACTIVE', NOW()),
  ('Étudiant Test', 'student@eduforge.local', 'password123', 'ETUDIANT', 'ACTIVE', NOW()),
  ('Manager Institution', 'institution@eduforge.local', 'password123', 'INSTITUTION_MANAGER', 'ACTIVE', NOW())
ON CONFLICT (email) DO NOTHING;

-- 2. Insert test institution (owned by institution manager = id 4)
INSERT INTO institution (owner_user_id, name, type, country, city, address, created_at)
SELECT 
  (SELECT id FROM app_user WHERE email = 'institution@eduforge.local'),
  'Université Ibn Tofail Meknès',
  'UNIVERSITE',
  'Maroc',
  'Meknès',
  'Avenue Ibn Tofail, Campus Universitaire',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM institution 
  WHERE owner_user_id = (SELECT id FROM app_user WHERE email = 'institution@eduforge.local')
);

-- 3. Insert test program
INSERT INTO program (institution_id, name)
SELECT 
  i.id,
  'Génie Informatique et Réseaux'
FROM institution i
WHERE i.owner_user_id = (SELECT id FROM app_user WHERE email = 'institution@eduforge.local')
AND NOT EXISTS (
  SELECT 1 FROM program p 
  WHERE p.institution_id = i.id 
  AND p.name = 'Génie Informatique et Réseaux'
);

-- 4. Insert test academic level
INSERT INTO academic_level (program_id, label)
SELECT 
  p.id,
  'L3 - Troisième année Licence'
FROM program p
INNER JOIN institution i ON i.id = p.institution_id
WHERE i.owner_user_id = (SELECT id FROM app_user WHERE email = 'institution@eduforge.local')
AND p.name = 'Génie Informatique et Réseaux'
AND NOT EXISTS (
  SELECT 1 FROM academic_level al 
  WHERE al.program_id = p.id 
  AND al.label = 'L3 - Troisième année Licence'
);

-- 5. Insert test subjects
INSERT INTO subject (level_id, name)
SELECT 
  al.id,
  subj.name
FROM academic_level al
INNER JOIN program p ON p.id = al.program_id
INNER JOIN institution i ON i.id = p.institution_id
CROSS JOIN (
  VALUES 
    ('Intelligence Artificielle'),
    ('Architecture Distribuée'),
    ('Sécurité Informatique'),
    ('Data Science')
) AS subj(name)
WHERE i.owner_user_id = (SELECT id FROM app_user WHERE email = 'institution@eduforge.local')
AND al.label = 'L3 - Troisième année Licence'
AND NOT EXISTS (
  SELECT 1 FROM subject s 
  WHERE s.level_id = al.id 
  AND s.name = subj.name
);

-- 6. Insert test classroom (owned by prof)
INSERT INTO classroom (owner_prof_id, institution_id, title, code, created_at)
SELECT 
  (SELECT id FROM app_user WHERE email = 'prof@eduforge.local'),
  (SELECT id FROM institution WHERE owner_user_id = (SELECT id FROM app_user WHERE email = 'institution@eduforge.local')),
  'IA & Machine Learning - Promo 2025',
  'IA2025ML',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM classroom WHERE code = 'IA2025ML'
);

-- 7. Enroll student in classroom
INSERT INTO classroom_enrollment (classroom_id, student_id, joined_at)
SELECT 
  c.id,
  (SELECT id FROM app_user WHERE email = 'student@eduforge.local'),
  NOW()
FROM classroom c
WHERE c.code = 'IA2025ML'
AND NOT EXISTS (
  SELECT 1 FROM classroom_enrollment ce 
  WHERE ce.classroom_id = c.id 
  AND ce.student_id = (SELECT id FROM app_user WHERE email = 'student@eduforge.local')
);

-- 8. Insert test course (owned by prof)
INSERT INTO course (owner_prof_id, title, description, text_content, status, created_at)
SELECT 
  (SELECT id FROM app_user WHERE email = 'prof@eduforge.local'),
  'Introduction au Machine Learning',
  'Cours complet sur les fondamentaux du ML : régression, classification, clustering',
  E'# Introduction au Machine Learning\n\nLe Machine Learning est une branche de l''IA qui permet aux systèmes d''apprendre à partir de données.\n\n## Types d''apprentissage\n\n1. **Supervisé** : Régression, Classification\n2. **Non-supervisé** : Clustering, Réduction de dimension\n3. **Par renforcement** : Q-Learning, Deep Q-Network\n\n## Algorithmes classiques\n\n- Régression linéaire\n- K-means\n- SVM\n- Réseaux de neurones',
  'PUBLISHED',
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM course 
  WHERE title = 'Introduction au Machine Learning'
);

-- 9. Link course to classroom
INSERT INTO classroom_course (classroom_id, course_id)
SELECT 
  c.id,
  co.id
FROM classroom c
CROSS JOIN course co
WHERE c.code = 'IA2025ML'
AND co.title = 'Introduction au Machine Learning'
AND NOT EXISTS (
  SELECT 1 FROM classroom_course cc 
  WHERE cc.classroom_id = c.id 
  AND cc.course_id = co.id
);
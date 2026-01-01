package com.eduforge.platform.service.course;

import com.eduforge.platform.domain.classroom.ClassroomEnrollment;
import com.eduforge.platform.domain.course.ClassroomCourse;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.course.CourseMaterial;
import com.eduforge.platform.domain.course.CourseStatus;
import com.eduforge.platform.repository.*;
import com.eduforge.platform.service.rag.RagIndexService;
import com.eduforge.platform.web.dto.forms.CourseCreateForm;
import com.eduforge.platform.web.dto.forms.CourseUpdateForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courses;
    private final CourseMaterialRepository materials;
    private final ClassroomCourseRepository classroomCourses;
    private final ClassroomEnrollmentRepository enrollments;
    private final ClassroomRepository classrooms;
    private final TextExtractionService extractor;
    private final RagIndexService ragIndexService;
    private final Path uploadsRoot;

    public CourseService(CourseRepository courses,
                         CourseMaterialRepository materials,
                         ClassroomCourseRepository classroomCourses,
                         ClassroomEnrollmentRepository enrollments,
                         ClassroomRepository classrooms,
                         TextExtractionService extractor,
                         RagIndexService ragIndexService,
                         Path uploadsRoot) {
        this.courses = courses;
        this.materials = materials;
        this.classroomCourses = classroomCourses;
        this.enrollments = enrollments;
        this.classrooms = classrooms;
        this.extractor = extractor;
        this.ragIndexService = ragIndexService;
        this.uploadsRoot = uploadsRoot;
    }

    public List<Course> profCourses(Long profId) {
        return courses.findByOwnerProfIdOrderByCreatedAtDesc(profId);
    }

    @Transactional
    public Course create(Long profId, CourseCreateForm form) {
        Course c = new Course(profId, form.getTitle().trim(),
                nullIfBlank(form.getDescription()),
                nullIfBlank(form.getTextContent()));
        return courses.save(c);
    }

    public Course requireOwned(Long courseId, Long profId) {
        return courses.findByIdAndOwnerProfId(courseId, profId)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable ou accès interdit."));
    }

    @Transactional
    public Course update(Long courseId, Long profId, CourseUpdateForm form) {
        Course c = requireOwned(courseId, profId);
        c.setTitle(form.getTitle().trim());
        c.setDescription(nullIfBlank(form.getDescription()));
        c.setTextContent(nullIfBlank(form.getTextContent()));
        
        // Auto-indexation RAG après mise à jour
        try {
            String fullText = fullCourseText(courseId);
            if (fullText != null && !fullText.isBlank()) {
                ragIndexService.reindexCourse(courseId, fullText);
                log.info("Cours {} auto-indexé après mise à jour", courseId);
            }
        } catch (Exception e) {
            log.warn("Échec auto-indexation RAG du cours {}: {}", courseId, e.getMessage());
            // Ne bloque pas la mise à jour du cours
        }
        
        return c;
    }

    public List<CourseMaterial> listMaterials(Long courseId) {
        return materials.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public CourseMaterial getMaterial(Long materialId) {
        return materials.findById(materialId).orElse(null);
    }

    @Transactional
    public CourseMaterial uploadMaterial(Long courseId, Long profId, MultipartFile file) {
        Course c = requireOwned(courseId, profId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide.");
        }

        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("upload.bin");
        String ext = extensionOf(original).toLowerCase(Locale.ROOT);

        String type;
        if (ext.equals("pdf")) type = "PDF";
        else if (ext.equals("pptx")) type = "PPTX";
        else if (ext.equals("txt")) type = "TEXT";
        else throw new IllegalArgumentException("Format non supporté. Autorisés : PDF, PPTX, TXT.");

        Path courseDir = uploadsRoot.resolve("courses").resolve(String.valueOf(c.getId()));
        try {
            Files.createDirectories(courseDir);
            String safeName = System.currentTimeMillis() + "-" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path stored = courseDir.resolve(safeName);
            Files.copy(file.getInputStream(), stored);

            String extracted = switch (type) {
                case "PDF" -> extractor.extractPdf(Files.newInputStream(stored));
                case "PPTX" -> extractor.extractPptx(Files.newInputStream(stored));
                default -> extractor.extractPlainText(Files.newInputStream(stored));
            };

            CourseMaterial m = new CourseMaterial(
                    c.getId(), type, original, stored.toString(), extracted
            );
            CourseMaterial saved = materials.save(m);
            
            // Auto-indexation RAG après ajout de matériel
            try {
                String fullText = fullCourseText(courseId);
                if (fullText != null && !fullText.isBlank()) {
                    ragIndexService.reindexCourse(courseId, fullText);
                    log.info("Cours {} auto-indexé après ajout de matériel", courseId);
                }
            } catch (Exception e) {
                log.warn("Échec auto-indexation RAG du cours {}: {}", courseId, e.getMessage());
                // Ne bloque pas l'upload du fichier
            }
            
            return saved;
        } catch (Exception e) {
            throw new IllegalArgumentException("Échec upload / extraction.", e);
        }
    }

    @Transactional
    public void attachToClassroom(Long courseId, Long profId, Long classroomId) {
        Course c = requireOwned(courseId, profId);

        // vérifie que cette classe appartient au prof
        classrooms.findById(classroomId)
                .filter(cl -> Objects.equals(cl.getOwnerProfId(), profId))
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable ou non autorisée."));

        if (!classroomCourses.existsByClassroomIdAndCourseId(classroomId, c.getId())) {
            classroomCourses.save(new ClassroomCourse(classroomId, c.getId()));
        }
    }

    @Transactional
    public void publish(Long courseId, Long profId) {
        publishWithType(courseId, profId, "PUBLIC", null);
    }

    @Transactional
    public void publishWithType(Long courseId, Long profId, String publishType, Long targetClassroomId) {
        Course c = requireOwned(courseId, profId);
        boolean hasText = c.getTextContent() != null && !c.getTextContent().isBlank();
        boolean hasMaterials = materials.countByCourseId(c.getId()) > 0;

        if (!hasText && !hasMaterials) {
            throw new IllegalArgumentException("Pour publier : ajoute du texte ou au moins un fichier PDF/PPTX/TXT.");
        }

        if ("CLASSE".equalsIgnoreCase(publishType)) {
            if (targetClassroomId == null) {
                throw new IllegalArgumentException("Pour publier en mode CLASSE, sélectionnez une classe.");
            }
            // Vérifier que la classe appartient au prof
            classrooms.findById(targetClassroomId)
                    .filter(cl -> Objects.equals(cl.getOwnerProfId(), profId))
                    .orElseThrow(() -> new IllegalArgumentException("Classe introuvable ou non autorisée."));
            
            c.setStatus(CourseStatus.CLASSE);
            c.setTargetClassroomId(targetClassroomId);
            
            // Automatiquement lier le cours à la classe
            if (!classroomCourses.existsByClassroomIdAndCourseId(targetClassroomId, c.getId())) {
                classroomCourses.save(new ClassroomCourse(targetClassroomId, c.getId()));
            }
        } else {
            c.setStatus(CourseStatus.PUBLIC);
            c.setTargetClassroomId(null);
        }
    }

    @Transactional
    public void unpublish(Long courseId, Long profId) {
        Course c = requireOwned(courseId, profId);
        c.setStatus(CourseStatus.DRAFT);
        c.setTargetClassroomId(null);
    }

    public Course getById(Long id) {
        return courses.findById(id).orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
    }

    public boolean studentCanAccessCourse(Long studentId, Long courseId) {
        // si l'étudiant est inscrit à au moins une classe ayant ce cours
        List<ClassroomEnrollment> enr = enrollments.findByStudentId(studentId);
        if (enr.isEmpty()) return false;
        Set<Long> studentClassIds = enr.stream().map(ClassroomEnrollment::getClassroomId).collect(Collectors.toSet());

        for (Long classId : studentClassIds) {
            if (classroomCourses.existsByClassroomIdAndCourseId(classId, courseId)) return true;
        }
        return false;
    }

    public List<Course> studentCourses(Long studentId) {
        List<ClassroomEnrollment> enr = enrollments.findByStudentId(studentId);
        if (enr.isEmpty()) return List.of();

        Set<Long> classIds = enr.stream().map(ClassroomEnrollment::getClassroomId).collect(Collectors.toSet());
        Set<Long> courseIds = new HashSet<>();
        for (Long classId : classIds) {
            classroomCourses.findByClassroomId(classId).forEach(cc -> courseIds.add(cc.getCourseId()));
        }
        if (courseIds.isEmpty()) return List.of();

        List<Course> found = courses.findAllById(courseIds);
        found.sort(Comparator.comparing(Course::getCreatedAt).reversed());
        return found;
    }

    public String fullCourseText(Long courseId) {
        Course c = getById(courseId);
        StringBuilder sb = new StringBuilder();
        if (c.getTextContent() != null && !c.getTextContent().isBlank()) {
            sb.append(c.getTextContent()).append("\n\n");
        }
        List<CourseMaterial> mats = listMaterials(courseId);
        for (CourseMaterial m : mats) {
            if (m.getContentText() != null && !m.getContentText().isBlank()) {
                sb.append("=== ").append(m.getType()).append(": ").append(m.getOriginalName()).append(" ===\n");
                sb.append(m.getContentText()).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    private String extensionOf(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        return name.substring(i + 1);
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}

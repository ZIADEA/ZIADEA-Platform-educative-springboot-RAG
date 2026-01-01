package com.eduforge.platform.service.classroom;

import com.eduforge.platform.domain.classroom.Classroom;
import com.eduforge.platform.domain.classroom.ClassroomEnrollment;
import com.eduforge.platform.domain.classroom.ClassroomPost;
import com.eduforge.platform.domain.course.ClassroomCourse;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClassroomService {

    private final ClassroomRepository classrooms;
    private final ClassroomEnrollmentRepository enrollments;
    private final ClassroomPostRepository posts;
    private final ClassroomCourseRepository classroomCourses;
    private final CourseRepository courses;
    private final UserRepository users;

    private final SecureRandom rnd = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    public ClassroomService(ClassroomRepository classrooms, 
                           ClassroomEnrollmentRepository enrollments,
                           ClassroomPostRepository posts,
                           ClassroomCourseRepository classroomCourses,
                           CourseRepository courses,
                           UserRepository users) {
        this.classrooms = classrooms;
        this.enrollments = enrollments;
        this.posts = posts;
        this.classroomCourses = classroomCourses;
        this.courses = courses;
        this.users = users;
    }

    public List<Classroom> profClasses(Long profId) {
        return classrooms.findByOwnerProfIdOrderByCreatedAtDesc(profId);
    }

    public Optional<Classroom> findById(Long classroomId) {
        return classrooms.findById(classroomId);
    }

    public Classroom getById(Long classroomId) {
        return classrooms.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
    }

    public Classroom requireOwned(Long classroomId, Long profId) {
        return classrooms.findById(classroomId)
                .filter(c -> c.getOwnerProfId().equals(profId))
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable ou non autorisée."));
    }

    @Transactional
    public Classroom createClass(Long profId, String title) {
        String code = generateCode(8);
        // très faible probabilité de collision, mais on sécurise
        while (classrooms.findByCodeIgnoreCase(code).isPresent()) {
            code = generateCode(8);
        }
        return classrooms.save(new Classroom(profId, null, title.trim(), code));
    }

    @Transactional
    public ClassroomEnrollment joinByCode(Long studentId, String code) {
        Classroom c = classrooms.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Code de classe invalide."));
        enrollments.findByClassroomIdAndStudentId(c.getId(), studentId)
                .ifPresent(e -> { throw new IllegalArgumentException("Tu es déjà inscrit à cette classe."); });

        return enrollments.save(new ClassroomEnrollment(c.getId(), studentId));
    }

    public List<ClassroomEnrollment> studentEnrollments(Long studentId) {
        return enrollments.findByStudentId(studentId);
    }

    // ===== Gestion des étudiants inscrits =====

    public List<ClassroomEnrollment> getEnrollments(Long classroomId) {
        return enrollments.findByClassroomId(classroomId);
    }

    public long countStudents(Long classroomId) {
        return enrollments.countByClassroomId(classroomId);
    }

    public List<User> getEnrolledStudents(Long classroomId) {
        List<ClassroomEnrollment> enr = enrollments.findByClassroomId(classroomId);
        List<Long> studentIds = enr.stream()
                .map(ClassroomEnrollment::getStudentId)
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) return List.of();
        return users.findAllById(studentIds);
    }

    @Transactional
    public void removeStudent(Long classroomId, Long studentId, Long profId) {
        requireOwned(classroomId, profId);
        enrollments.findByClassroomIdAndStudentId(classroomId, studentId)
                .ifPresent(enrollments::delete);
    }

    // ===== Gestion des publications (posts) =====

    public List<ClassroomPost> getPosts(Long classroomId) {
        return posts.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }

    @Transactional
    public ClassroomPost createPost(Long classroomId, Long authorId, String type, String content) {
        if (content == null || content.trim().isBlank()) {
            throw new IllegalArgumentException("Le contenu ne peut pas être vide.");
        }
        ClassroomPost post = new ClassroomPost(classroomId, authorId, type, content.trim());
        return posts.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long profId) {
        posts.findById(postId).ifPresent(post -> {
            // Vérifie que le prof est propriétaire de la classe
            requireOwned(post.getClassroomId(), profId);
            posts.delete(post);
        });
    }

    // ===== Gestion des cours liés =====

    public List<Course> getClassroomCourses(Long classroomId) {
        List<ClassroomCourse> links = classroomCourses.findByClassroomId(classroomId);
        List<Long> courseIds = links.stream()
                .map(ClassroomCourse::getCourseId)
                .collect(Collectors.toList());
        if (courseIds.isEmpty()) return List.of();
        return courses.findAllById(courseIds);
    }

    public long countCourses(Long classroomId) {
        return classroomCourses.countByClassroomId(classroomId);
    }

    @Transactional
    public void attachCourse(Long classroomId, Long courseId, Long profId) {
        requireOwned(classroomId, profId);
        Course course = courses.findById(courseId)
                .filter(c -> c.getOwnerProfId().equals(profId))
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable ou non autorisé."));

        if (!classroomCourses.existsByClassroomIdAndCourseId(classroomId, courseId)) {
            classroomCourses.save(new ClassroomCourse(classroomId, courseId));
        }
    }

    @Transactional
    public void detachCourse(Long classroomId, Long courseId, Long profId) {
        requireOwned(classroomId, profId);
        classroomCourses.findByClassroomIdAndCourseId(classroomId, courseId)
                .ifPresent(classroomCourses::delete);
    }

    // ===== Utilitaires =====

    private String generateCode(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHABET.charAt(rnd.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public User getOwner(Classroom classroom) {
        return users.findById(classroom.getOwnerProfId()).orElse(null);
    }
}

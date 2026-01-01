package com.eduforge.platform.service.course;

import com.eduforge.platform.domain.course.*;
import com.eduforge.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollments;
    private final CourseRepository courses;
    private final CourseChapterRepository chapters;

    public CourseEnrollmentService(CourseEnrollmentRepository enrollments,
                                   CourseRepository courses,
                                   CourseChapterRepository chapters) {
        this.enrollments = enrollments;
        this.courses = courses;
        this.chapters = chapters;
    }

    public List<CourseEnrollment> getStudentEnrollments(Long studentId) {
        return enrollments.findByStudentIdOrderByEnrolledAtDesc(studentId);
    }

    public List<CourseEnrollment> getCourseEnrollments(Long courseId) {
        return enrollments.findByCourseIdOrderByEnrolledAtDesc(courseId);
    }

    public Optional<CourseEnrollment> getEnrollment(Long courseId, Long studentId) {
        return enrollments.findByCourseIdAndStudentId(courseId, studentId);
    }

    public boolean isEnrolled(Long courseId, Long studentId) {
        return enrollments.existsByCourseIdAndStudentId(courseId, studentId);
    }

    public long getEnrollmentCount(Long courseId) {
        return enrollments.countByCourseId(courseId);
    }

    public long getCompletedCount(Long courseId) {
        return enrollments.countByCourseIdAndIsCompletedTrue(courseId);
    }

    @Transactional
    public CourseEnrollment enroll(Long courseId, Long studentId) {
        if (enrollments.existsByCourseIdAndStudentId(courseId, studentId)) {
            return enrollments.findByCourseIdAndStudentId(courseId, studentId).orElse(null);
        }

        Course course = courses.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));

        CourseEnrollment enrollment = new CourseEnrollment(courseId, studentId);
        return enrollments.save(enrollment);
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        enrollments.deleteByCourseIdAndStudentId(courseId, studentId);
    }

    @Transactional
    public CourseEnrollment markChapterComplete(Long courseId, Long studentId, Long chapterId) {
        CourseEnrollment enrollment = enrollments.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable."));

        // Ajouter le chapitre complété
        Set<Long> completed = enrollment.getCompletedChapterIds();
        completed.add(chapterId);
        enrollment.setCompletedChapterIds(completed);
        enrollment.setLastAccessedChapterId(chapterId);
        enrollment.setLastAccessedAt(Instant.now());

        // Calculer la progression
        int totalChapters = chapters.findByCourseIdAndIsPublishedTrueOrderByChapterOrderAsc(courseId).size();
        int completedCount = completed.size();
        int progress = totalChapters > 0 ? (completedCount * 100) / totalChapters : 0;
        enrollment.setProgressPercent(progress);

        // Vérifier si le cours est terminé
        if (progress >= 100) {
            enrollment.setIsCompleted(true);
            enrollment.setCompletedAt(Instant.now());
        }

        return enrollments.save(enrollment);
    }

    @Transactional
    public CourseEnrollment updateLastAccessed(Long courseId, Long studentId, Long chapterId) {
        CourseEnrollment enrollment = enrollments.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable."));

        enrollment.setLastAccessedChapterId(chapterId);
        enrollment.setLastAccessedAt(Instant.now());

        return enrollments.save(enrollment);
    }

    public record EnrollmentStats(
            long totalEnrollments,
            long completedCount,
            double avgProgress,
            long activeThisWeek
    ) {}

    public EnrollmentStats getCourseStats(Long courseId) {
        long total = enrollments.countByCourseId(courseId);
        long completed = enrollments.countByCourseIdAndIsCompletedTrue(courseId);
        Double avgProgress = enrollments.averageProgressByCourseId(courseId);
        Instant weekAgo = Instant.now().minusSeconds(7 * 24 * 60 * 60);
        long active = enrollments.countActiveSince(courseId, weekAgo);

        return new EnrollmentStats(
                total,
                completed,
                avgProgress != null ? avgProgress : 0,
                active
        );
    }
}

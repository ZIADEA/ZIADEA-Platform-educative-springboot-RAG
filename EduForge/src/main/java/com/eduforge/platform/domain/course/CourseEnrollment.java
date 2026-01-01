package com.eduforge.platform.domain.course;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Inscription d'un étudiant à un cours public
 */
@Entity
@Table(name = "course_enrollment", indexes = {
    @Index(name = "idx_ce_course", columnList = "course_id"),
    @Index(name = "idx_ce_student", columnList = "student_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "student_id"})
})
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "progress_percent")
    private Integer progressPercent = 0;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_accessed_at")
    private Instant lastAccessedAt;

    @Column(name = "last_accessed_chapter_id")
    private Long lastAccessedChapterId;

    @ElementCollection
    @CollectionTable(name = "enrollment_completed_chapters",
            joinColumns = @JoinColumn(name = "enrollment_id"))
    @Column(name = "chapter_id")
    private Set<Long> completedChapterIds = new HashSet<>();

    public CourseEnrollment() {}

    public CourseEnrollment(Long courseId, Long studentId) {
        this.courseId = courseId;
        this.studentId = studentId;
        this.enrolledAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    public Long getLastAccessedChapterId() { return lastAccessedChapterId; }
    public void setLastAccessedChapterId(Long lastAccessedChapterId) { this.lastAccessedChapterId = lastAccessedChapterId; }

    public Set<Long> getCompletedChapterIds() { return completedChapterIds; }
    public void setCompletedChapterIds(Set<Long> completedChapterIds) { this.completedChapterIds = completedChapterIds; }

    public void markCompleted() {
        this.isCompleted = true;
        this.completedAt = Instant.now();
        this.progressPercent = 100;
    }

    public void updateAccess(Long chapterId) {
        this.lastAccessedAt = Instant.now();
        this.lastAccessedChapterId = chapterId;
    }
}

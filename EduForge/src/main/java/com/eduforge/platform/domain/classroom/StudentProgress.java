package com.eduforge.platform.domain.classroom;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Progression d'un étudiant dans une classe (gamification)
 */
@Entity
@Table(name = "student_progress", indexes = {
    @Index(name = "idx_sp_student", columnList = "student_id"),
    @Index(name = "idx_sp_classroom", columnList = "classroom_id")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"classroom_id", "student_id"})
})
public class StudentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "current_level_id")
    private Long currentLevelId;

    @Column(name = "xp_points")
    private Integer xpPoints = 0;

    @Column(name = "total_activities")
    private Integer totalActivities = 0;

    @Column(name = "quizzes_attempted")
    private Integer quizzesAttempted = 0;

    @Column(name = "quizzes_passed")
    private Integer quizzesPassed = 0;

    @Column(name = "exams_attempted")
    private Integer examsAttempted = 0;

    @Column(name = "exams_passed")
    private Integer examsPassed = 0;

    @Column(name = "assignments_submitted")
    private Integer assignmentsSubmitted = 0;

    @Column(name = "average_score")
    private Double averageScore = 0.0;

    @ElementCollection
    @CollectionTable(name = "student_badges",
            joinColumns = @JoinColumn(name = "progress_id"))
    @Column(name = "badge")
    private Set<String> badges = new HashSet<>();

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    public StudentProgress() {}

    public StudentProgress(Long classroomId, Long studentId) {
        this.classroomId = classroomId;
        this.studentId = studentId;
        this.startedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCurrentLevelId() { return currentLevelId; }
    public void setCurrentLevelId(Long currentLevelId) { this.currentLevelId = currentLevelId; }

    public Integer getXpPoints() { return xpPoints; }
    public void setXpPoints(Integer xpPoints) { this.xpPoints = xpPoints; }

    public Integer getTotalActivities() { return totalActivities; }
    public void setTotalActivities(Integer totalActivities) { this.totalActivities = totalActivities; }

    public Integer getQuizzesAttempted() { return quizzesAttempted; }
    public void setQuizzesAttempted(Integer quizzesAttempted) { this.quizzesAttempted = quizzesAttempted; }

    public Integer getQuizzesPassed() { return quizzesPassed; }
    public void setQuizzesPassed(Integer quizzesPassed) { this.quizzesPassed = quizzesPassed; }

    public Integer getExamsAttempted() { return examsAttempted; }
    public void setExamsAttempted(Integer examsAttempted) { this.examsAttempted = examsAttempted; }

    public Integer getExamsPassed() { return examsPassed; }
    public void setExamsPassed(Integer examsPassed) { this.examsPassed = examsPassed; }

    public Integer getAssignmentsSubmitted() { return assignmentsSubmitted; }
    public void setAssignmentsSubmitted(Integer assignmentsSubmitted) { this.assignmentsSubmitted = assignmentsSubmitted; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Set<String> getBadges() { return badges; }
    public void setBadges(Set<String> badges) { this.badges = badges; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(Instant lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    // Helper methods
    public double getQuizSuccessRate() {
        if (quizzesAttempted == 0) return 0;
        return (double) quizzesPassed / quizzesAttempted * 100;
    }

    public double getExamSuccessRate() {
        if (examsAttempted == 0) return 0;
        return (double) examsPassed / examsAttempted * 100;
    }
}

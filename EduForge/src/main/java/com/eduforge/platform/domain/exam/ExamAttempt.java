package com.eduforge.platform.domain.exam;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Tentative d'examen par un étudiant
 */
@Entity
@Table(name = "exam_attempt", indexes = {
    @Index(name = "idx_ea_exam", columnList = "exam_id"),
    @Index(name = "idx_ea_student", columnList = "student_id"),
    @Index(name = "idx_ea_status", columnList = "status")
})
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "score_percent")
    private Integer scorePercent;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "total_count")
    private Integer totalCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds = 0;

    @Column(name = "attempt_number")
    private Integer attemptNumber = 1;

    public ExamAttempt() {}

    public ExamAttempt(Long examId, Long studentId, Integer attemptNumber) {
        this.examId = examId;
        this.studentId = studentId;
        this.attemptNumber = attemptNumber;
        this.status = AttemptStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getScorePercent() { return scorePercent; }
    public void setScorePercent(Integer scorePercent) { this.scorePercent = scorePercent; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }

    public Integer getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(Integer timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public boolean isPassed(int threshold) {
        return scorePercent != null && scorePercent >= threshold;
    }
}

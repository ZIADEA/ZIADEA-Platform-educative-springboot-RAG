package com.eduforge.platform.domain.quiz;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="quiz_attempt", indexes = {
        @Index(name="idx_attempt_student_course", columnList="student_id,course_id"),
        @Index(name="idx_attempt_quiz", columnList="quiz_id")
})
public class QuizAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="quiz_id", nullable = false)
    private Long quizId;

    @Column(name="course_id", nullable = false)
    private Long courseId;

    @Column(name="student_id", nullable = false)
    private Long studentId;

    @Column(name="score_percent", nullable = false)
    private int scorePercent;

    @Column(name="correct_count", nullable = false)
    private int correctCount;

    @Column(name="total_count", nullable = false)
    private int totalCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status;

    @Column(name="created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public QuizAttempt() {}

    public QuizAttempt(Long quizId, Long courseId, Long studentId,
                       int scorePercent, int correctCount, int totalCount, AttemptStatus status) {
        this.quizId = quizId;
        this.courseId = courseId;
        this.studentId = studentId;
        this.scorePercent = scorePercent;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getQuizId() { return quizId; }
    public Long getCourseId() { return courseId; }
    public Long getStudentId() { return studentId; }
    public int getScorePercent() { return scorePercent; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalCount() { return totalCount; }
    public AttemptStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public void setScorePercent(int scorePercent) { this.scorePercent = scorePercent; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

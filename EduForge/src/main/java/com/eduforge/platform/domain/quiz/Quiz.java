package com.eduforge.platform.domain.quiz;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "quiz")
public class Quiz {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="course_id", nullable = false)
    private Long courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name="question_count", nullable = false)
    private int questionCount;

    @Column(name="created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Quiz() {}

    public Quiz(Long courseId, Difficulty difficulty, int questionCount) {
        this.courseId = courseId;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getQuestionCount() { return questionCount; }
    public Instant getCreatedAt() { return createdAt; }

    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

package com.eduforge.platform.web.dto;

import java.time.Instant;

/**
 * DTO pour afficher les tentatives d'examen dans le profil étudiant
 */
public class ExamAttemptDTO {
    
    private Long id;
    private String examTitle;
    private Instant completedAt;
    private Integer score;
    private boolean passed;
    private String status;

    public ExamAttemptDTO() {}

    public ExamAttemptDTO(Long id, String examTitle, Instant completedAt, Integer score, boolean passed, String status) {
        this.id = id;
        this.examTitle = examTitle;
        this.completedAt = completedAt;
        this.score = score;
        this.passed = passed;
        this.status = status;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

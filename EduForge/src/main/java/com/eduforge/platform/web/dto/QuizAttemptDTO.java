package com.eduforge.platform.web.dto;

import java.time.Instant;

/**
 * DTO pour afficher les tentatives de quiz dans le profil étudiant
 */
public class QuizAttemptDTO {
    
    private Long id;
    private String quizTitle;
    private Instant completedAt;
    private int correctAnswers;
    private int totalQuestions;
    private int scorePercent;

    public QuizAttemptDTO() {}

    public QuizAttemptDTO(Long id, String quizTitle, Instant completedAt, int correctAnswers, int totalQuestions, int scorePercent) {
        this.id = id;
        this.quizTitle = quizTitle;
        this.completedAt = completedAt;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.scorePercent = scorePercent;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getScorePercent() { return scorePercent; }
    public void setScorePercent(int scorePercent) { this.scorePercent = scorePercent; }
}

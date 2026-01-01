package com.eduforge.platform.domain.exam;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Réponse à une question d'examen
 */
@Entity
@Table(name = "exam_answer", indexes = {
    @Index(name = "idx_exa_attempt", columnList = "attempt_id")
})
public class ExamAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    // Pour QCM
    @Column(name = "chosen_choice", length = 1)
    private String chosenChoice;

    // Pour réponse ouverte
    @Column(name = "text_answer", columnDefinition = "text")
    private String textAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;

    @Column(name = "ai_score")
    private Integer aiScore; // Score donné par l'IA (0-100)

    @Column(name = "ai_feedback", columnDefinition = "text")
    private String aiFeedback; // Feedback de l'IA

    @Column(name = "answered_at")
    private Instant answeredAt;

    public ExamAnswer() {}

    // Constructeur pour QCM
    public ExamAnswer(Long attemptId, Long questionId, String chosenChoice, Boolean isCorrect) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.chosenChoice = chosenChoice;
        this.isCorrect = isCorrect;
        this.answeredAt = Instant.now();
    }

    // Constructeur pour réponse ouverte
    public ExamAnswer(Long attemptId, Long questionId, String textAnswer) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.textAnswer = textAnswer;
        this.answeredAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public String getChosenChoice() { return chosenChoice; }
    public void setChosenChoice(String chosenChoice) { this.chosenChoice = chosenChoice; }

    public String getTextAnswer() { return textAnswer; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getAiScore() { return aiScore; }
    public void setAiScore(Integer aiScore) { this.aiScore = aiScore; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }
}

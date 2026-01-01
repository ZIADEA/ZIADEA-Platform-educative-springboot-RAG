package com.eduforge.platform.domain.quiz;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="quiz_attempt_answer", indexes = @Index(name="idx_ans_attempt", columnList="attempt_id"))
public class QuizAttemptAnswer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="attempt_id", nullable = false)
    private Long attemptId;

    @Column(name="question_id", nullable = false)
    private Long questionId;

    // Pour QCM
    @Column(name="chosen_choice", length = 1)
    private String chosenChoice;

    @Column(name="is_correct", nullable = false)
    private boolean correct;

    // Pour questions ouvertes
    @Column(name="text_answer", columnDefinition = "text")
    private String textAnswer;

    @Column(name="ai_score", precision = 5, scale = 2)
    private BigDecimal aiScore; // Score attribué par l'IA (0-100 ou 0-maxPoints)

    @Column(name="ai_feedback", columnDefinition = "text")
    private String aiFeedback; // Feedback de l'IA sur la réponse

    public QuizAttemptAnswer() {}

    // Constructeur QCM
    public QuizAttemptAnswer(Long attemptId, Long questionId, String chosenChoice, boolean correct) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.chosenChoice = chosenChoice;
        this.correct = correct;
    }

    // Constructeur question ouverte
    public QuizAttemptAnswer(Long attemptId, Long questionId, String textAnswer, 
                             BigDecimal aiScore, String aiFeedback, boolean correct) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.textAnswer = textAnswer;
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.correct = correct;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAttemptId() { return attemptId; }
    public Long getQuestionId() { return questionId; }
    public String getChosenChoice() { return chosenChoice; }
    public boolean isCorrect() { return correct; }
    public String getTextAnswer() { return textAnswer; }
    public BigDecimal getAiScore() { return aiScore; }
    public String getAiFeedback() { return aiFeedback; }

    // Setters
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public void setChosenChoice(String chosenChoice) { this.chosenChoice = chosenChoice; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public void setTextAnswer(String textAnswer) { this.textAnswer = textAnswer; }
    public void setAiScore(BigDecimal aiScore) { this.aiScore = aiScore; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
}

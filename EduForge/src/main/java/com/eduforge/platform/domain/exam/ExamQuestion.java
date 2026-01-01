package com.eduforge.platform.domain.exam;

import com.eduforge.platform.domain.quiz.QuestionType;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Question d'un examen (QCM ou réponse ouverte)
 */
@Entity
@Table(name = "exam_question", indexes = {
    @Index(name = "idx_eq_exam", columnList = "exam_id")
})
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "q_index", nullable = false)
    private Integer qIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType = QuestionType.MCQ;

    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;

    // Pour les QCM
    @Column(name = "a_text", columnDefinition = "text")
    private String aText;

    @Column(name = "b_text", columnDefinition = "text")
    private String bText;

    @Column(name = "c_text", columnDefinition = "text")
    private String cText;

    @Column(name = "d_text", columnDefinition = "text")
    private String dText;

    @Column(name = "correct_choice", length = 1)
    private String correctChoice;

    // Pour les questions ouvertes
    @Column(name = "expected_answer", columnDefinition = "text")
    private String expectedAnswer;

    @Column(name = "grading_rubric", columnDefinition = "text")
    private String gradingRubric;

    @Column(columnDefinition = "text")
    private String explanation;

    @Column(precision = 4, scale = 2)
    private BigDecimal points = new BigDecimal("1.00");

    public ExamQuestion() {}

    // Constructeur pour QCM
    public ExamQuestion(Long examId, Integer qIndex, String questionText, 
                        String aText, String bText, String cText, String dText,
                        String correctChoice, String explanation) {
        this.examId = examId;
        this.qIndex = qIndex;
        this.questionType = QuestionType.MCQ;
        this.questionText = questionText;
        this.aText = aText;
        this.bText = bText;
        this.cText = cText;
        this.dText = dText;
        this.correctChoice = correctChoice;
        this.explanation = explanation;
    }

    // Constructeur pour questions ouvertes
    public ExamQuestion(Long examId, Integer qIndex, String questionText, 
                        String expectedAnswer, String gradingRubric) {
        this.examId = examId;
        this.qIndex = qIndex;
        this.questionType = QuestionType.OPEN_ENDED;
        this.questionText = questionText;
        this.expectedAnswer = expectedAnswer;
        this.gradingRubric = gradingRubric;
    }

    public boolean isMCQ() {
        return questionType == QuestionType.MCQ;
    }

    public boolean isOpenEnded() {
        return questionType == QuestionType.OPEN_ENDED;
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Integer getQIndex() { return qIndex; }
    public void setQIndex(Integer qIndex) { this.qIndex = qIndex; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAText() { return aText; }
    public void setAText(String aText) { this.aText = aText; }

    public String getBText() { return bText; }
    public void setBText(String bText) { this.bText = bText; }

    public String getCText() { return cText; }
    public void setCText(String cText) { this.cText = cText; }

    public String getDText() { return dText; }
    public void setDText(String dText) { this.dText = dText; }

    public String getCorrectChoice() { return correctChoice; }
    public void setCorrectChoice(String correctChoice) { this.correctChoice = correctChoice; }

    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }

    public String getGradingRubric() { return gradingRubric; }
    public void setGradingRubric(String gradingRubric) { this.gradingRubric = gradingRubric; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public BigDecimal getPoints() { return points; }
    public void setPoints(BigDecimal points) { this.points = points; }

    public String getChoiceText(String choice) {
        return switch (choice.toUpperCase()) {
            case "A" -> aText;
            case "B" -> bText;
            case "C" -> cText;
            case "D" -> dText;
            default -> "";
        };
    }
}

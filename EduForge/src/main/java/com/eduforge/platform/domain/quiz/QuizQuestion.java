package com.eduforge.platform.domain.quiz;

import jakarta.persistence.*;

@Entity
@Table(name="quiz_question", indexes = @Index(name="idx_q_qz", columnList="quiz_id"))
public class QuizQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="quiz_id", nullable = false)
    private Long quizId;

    @Column(name="q_index", nullable = false)
    private int index;

    @Enumerated(EnumType.STRING)
    @Column(name="question_type", length = 20)
    private QuestionType questionType = QuestionType.MCQ;

    @Column(name="question_text", columnDefinition = "text", nullable = false)
    private String questionText;

    // Champs pour QCM (nullable pour questions ouvertes)
    @Column(name="a_text", columnDefinition = "text")
    private String aText;

    @Column(name="b_text", columnDefinition = "text")
    private String bText;

    @Column(name="c_text", columnDefinition = "text")
    private String cText;

    @Column(name="d_text", columnDefinition = "text")
    private String dText;

    @Column(name="correct_choice", length = 1)
    private String correctChoice; // A/B/C/D pour QCM

    // Champs pour questions ouvertes
    @Column(name="expected_answer", columnDefinition = "text")
    private String expectedAnswer; // Réponse attendue (guide pour l'IA)

    @Column(name="grading_criteria", columnDefinition = "text")
    private String gradingCriteria; // Critères de notation pour l'IA

    @Column(name="max_points")
    private Integer maxPoints; // Points max pour questions ouvertes

    @Column(name="explanation", columnDefinition = "text", nullable = false)
    private String explanation;

    public QuizQuestion() {}

    // Constructeur QCM
    public QuizQuestion(Long quizId, int index, String questionText,
                        String aText, String bText, String cText, String dText,
                        String correctChoice, String explanation) {
        this.quizId = quizId;
        this.index = index;
        this.questionType = QuestionType.MCQ;
        this.questionText = questionText;
        this.aText = aText;
        this.bText = bText;
        this.cText = cText;
        this.dText = dText;
        this.correctChoice = correctChoice;
        this.explanation = explanation;
    }

    // Constructeur question ouverte
    public QuizQuestion(Long quizId, int index, String questionText,
                        String expectedAnswer, String gradingCriteria, 
                        Integer maxPoints, String explanation) {
        this.quizId = quizId;
        this.index = index;
        this.questionType = QuestionType.OPEN_ENDED;
        this.questionText = questionText;
        this.expectedAnswer = expectedAnswer;
        this.gradingCriteria = gradingCriteria;
        this.maxPoints = maxPoints != null ? maxPoints : 10;
        this.explanation = explanation;
    }
    
    // Static factory method for open-ended questions
    public static QuizQuestion openEnded(Long quizId, int index, String questionText,
                                         String expectedAnswer, String gradingCriteria,
                                         int maxPoints, String explanation) {
        return new QuizQuestion(quizId, index, questionText, expectedAnswer, gradingCriteria, maxPoints, explanation);
    }

    // Getters
    public Long getId() { return id; }
    public Long getQuizId() { return quizId; }
    public int getIndex() { return index; }
    public QuestionType getQuestionType() { return questionType; }
    public String getQuestionText() { return questionText; }
    public String getAText() { return aText; }
    public String getBText() { return bText; }
    public String getCText() { return cText; }
    public String getDText() { return dText; }
    public String getCorrectChoice() { return correctChoice; }
    public String getExpectedAnswer() { return expectedAnswer; }
    public String getGradingCriteria() { return gradingCriteria; }
    public Integer getMaxPoints() { return maxPoints != null ? maxPoints : 10; }
    public String getExplanation() { return explanation; }

    // Setters
    public void setQuizId(Long quizId) { this.quizId = quizId; }
    public void setIndex(int index) { this.index = index; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setAText(String aText) { this.aText = aText; }
    public void setBText(String bText) { this.bText = bText; }
    public void setCText(String cText) { this.cText = cText; }
    public void setDText(String dText) { this.dText = dText; }
    public void setCorrectChoice(String correctChoice) { this.correctChoice = correctChoice; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }
    public void setGradingCriteria(String gradingCriteria) { this.gradingCriteria = gradingCriteria; }
    public void setMaxPoints(Integer maxPoints) { this.maxPoints = maxPoints; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    // Helper methods
    public boolean isMcq() { return questionType == QuestionType.MCQ; }
    public boolean isOpenEnded() { return questionType == QuestionType.OPEN_ENDED; }
}

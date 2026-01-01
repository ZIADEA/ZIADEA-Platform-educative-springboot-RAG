package com.eduforge.platform.web.dto.forms;

import java.util.HashMap;
import java.util.Map;

public class QuizSubmitForm {
    private Long quizId;
    private Long courseId;
    private Map<Long, String> answers = new HashMap<>();

    public Long getQuizId() { return quizId; }
    public void setQuizId(Long quizId) { this.quizId = quizId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Map<Long, String> getAnswers() { return answers; }
    public void setAnswers(Map<Long, String> answers) { this.answers = answers; }
}

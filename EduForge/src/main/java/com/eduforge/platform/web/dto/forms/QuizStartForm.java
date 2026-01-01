package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class QuizStartForm {
    @Size(max = 250)
    private String query;
    
    @Min(3)
    @Max(20)
    private Integer questionCount;
    
    private boolean includeOpenEnded = false;
    
    private Integer openEndedCount = 0;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    
    public boolean isIncludeOpenEnded() { return includeOpenEnded; }
    public void setIncludeOpenEnded(boolean includeOpenEnded) { this.includeOpenEnded = includeOpenEnded; }
    
    public Integer getOpenEndedCount() { return openEndedCount; }
    public void setOpenEndedCount(Integer openEndedCount) { this.openEndedCount = openEndedCount; }
}

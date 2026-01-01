package com.eduforge.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.quiz")
public class QuizProperties {
    private int defaultPassThreshold = 70;
    private int minQuestions = 6;
    private int maxQuestions = 10;

    public int getDefaultPassThreshold() { return defaultPassThreshold; }
    public void setDefaultPassThreshold(int defaultPassThreshold) { this.defaultPassThreshold = defaultPassThreshold; }

    public int getMinQuestions() { return minQuestions; }
    public void setMinQuestions(int minQuestions) { this.minQuestions = minQuestions; }

    public int getMaxQuestions() { return maxQuestions; }
    public void setMaxQuestions(int maxQuestions) { this.maxQuestions = maxQuestions; }
}

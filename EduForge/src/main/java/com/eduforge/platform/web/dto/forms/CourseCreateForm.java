package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CourseCreateForm {
    @NotBlank @Size(max = 180)
    private String title;

    @Size(max = 800)
    private String description;

    private String textContent;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
}

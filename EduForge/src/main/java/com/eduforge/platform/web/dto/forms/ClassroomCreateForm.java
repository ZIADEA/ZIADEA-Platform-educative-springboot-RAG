package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClassroomCreateForm {
    @NotBlank @Size(max = 160)
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}

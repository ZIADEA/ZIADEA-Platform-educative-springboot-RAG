package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SubjectForm {
    @NotBlank @Size(max = 120)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LevelForm {
    @NotBlank @Size(max = 80)
    private String label;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}

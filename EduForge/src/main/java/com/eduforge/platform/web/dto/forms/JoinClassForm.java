package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinClassForm {
    @NotBlank @Size(min = 6, max = 16)
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
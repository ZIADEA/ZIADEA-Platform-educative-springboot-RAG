package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotNull;

public class AffiliationRequestForm {
    
    @NotNull(message = "Veuillez sélectionner une institution")
    private Long institutionId;
    
    private String message;

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

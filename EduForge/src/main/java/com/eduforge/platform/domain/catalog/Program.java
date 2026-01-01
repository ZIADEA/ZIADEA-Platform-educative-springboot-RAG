package com.eduforge.platform.domain.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "program")
public class Program {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String name;

    public Program() {}

    public Program(Long institutionId, String name) {
        this.institutionId = institutionId;
        this.name = name;
    }

    public Long getId() { return id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

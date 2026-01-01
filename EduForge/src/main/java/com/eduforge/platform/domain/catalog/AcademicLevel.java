package com.eduforge.platform.domain.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "academic_level")
public class AcademicLevel {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String label; // ex: L1, 1ère année, Terminale

    public AcademicLevel() {}

    public AcademicLevel(Long programId, String label) {
        this.programId = programId;
        this.label = label;
    }

    public Long getId() { return id; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}

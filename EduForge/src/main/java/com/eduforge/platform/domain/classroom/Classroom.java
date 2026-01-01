package com.eduforge.platform.domain.classroom;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(name = "classroom",
       uniqueConstraints = @UniqueConstraint(name = "uq_class_code", columnNames = "code"))
public class Classroom {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_prof_id", nullable = false)
    private Long ownerProfId;

    @Column(name = "institution_id")
    private Long institutionId; // nullable : prof libre

    @Column(name = "subject_id")
    private Long subjectId; // Matière associée à cette classe

    @NotBlank
    @Column(nullable = false, length = 160)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 16)
    private String code;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Classroom() {}

    public Classroom(Long ownerProfId, Long institutionId, String title, String code) {
        this.ownerProfId = ownerProfId;
        this.institutionId = institutionId;
        this.title = title;
        this.code = code;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getOwnerProfId() { return ownerProfId; }
    public void setOwnerProfId(Long ownerProfId) { this.ownerProfId = ownerProfId; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

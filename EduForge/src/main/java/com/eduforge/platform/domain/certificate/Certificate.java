package com.eduforge.platform.domain.certificate;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Certificat de complétion
 */
@Entity
@Table(name = "certificate", indexes = {
    @Index(name = "idx_cert_student", columnList = "student_id"),
    @Index(name = "idx_cert_code", columnList = "certificate_code")
})
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "certificate_type", nullable = false, length = 30)
    private String certificateType = "COMPLETION";

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    @Column(name = "certificate_code", unique = true, length = 50)
    private String certificateCode;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    public Certificate() {
        this.certificateCode = generateCode();
    }

    public Certificate(Long studentId, String title, String certificateType) {
        this.studentId = studentId;
        this.title = title;
        this.certificateType = certificateType;
        this.certificateCode = generateCode();
        this.issuedAt = Instant.now();
    }

    private String generateCode() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public String getCertificateType() { return certificateType; }
    public void setCertificateType(String certificateType) { this.certificateType = certificateType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public String getCertificateCode() { return certificateCode; }
    public void setCertificateCode(String certificateCode) { this.certificateCode = certificateCode; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
}

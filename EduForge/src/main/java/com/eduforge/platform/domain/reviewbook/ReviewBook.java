package com.eduforge.platform.domain.reviewbook;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * ReviewBook: document uploadé par un étudiant pour révision personnalisée.
 * L'IA extrait le texte (OCR si image) et génère des quiz personnalisés.
 */
@Entity
@Table(name = "review_book", indexes = {
    @Index(name = "idx_rb_student", columnList = "student_id"),
    @Index(name = "idx_rb_created", columnList = "created_at")
})
public class ReviewBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_path", nullable = false)
    private String storedPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private ReviewBookFileType fileType;

    @Column(name = "extracted_text", columnDefinition = "text")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewBookStatus status = ReviewBookStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    public ReviewBook() {}

    public ReviewBook(Long studentId, String title, String originalFilename, 
                      String storedPath, ReviewBookFileType fileType) {
        this.studentId = studentId;
        this.title = title;
        this.originalFilename = originalFilename;
        this.storedPath = storedPath;
        this.fileType = fileType;
        this.status = ReviewBookStatus.PENDING;
    }

    // Getters
    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredPath() { return storedPath; }
    public ReviewBookFileType getFileType() { return fileType; }
    public String getExtractedText() { return extractedText; }
    public ReviewBookStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public void setStatus(ReviewBookStatus status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    // Helper methods
    public boolean isReady() { return status == ReviewBookStatus.READY; }
    public boolean hasFailed() { return status == ReviewBookStatus.FAILED; }
    public boolean isPending() { return status == ReviewBookStatus.PENDING; }
}

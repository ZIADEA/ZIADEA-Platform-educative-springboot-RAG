package com.eduforge.platform.domain.library;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Ressource de bibliothèque institutionnelle (PDF, livres, documents)
 */
@Entity
@Table(name = "library_resource", indexes = {
    @Index(name = "idx_lib_inst", columnList = "institution_id"),
    @Index(name = "idx_lib_category", columnList = "category")
})
public class LibraryResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 80)
    private String category;

    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType = "PDF";

    @NotBlank
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @NotBlank
    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(length = 180)
    private String author;

    @Column(length = 30)
    private String isbn;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public LibraryResource() {}

    public LibraryResource(Long institutionId, String title, String originalName, 
                          String storedPath, String fileType, Long uploadedBy) {
        this.institutionId = institutionId;
        this.title = title;
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.createdAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void incrementDownloads() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }
}

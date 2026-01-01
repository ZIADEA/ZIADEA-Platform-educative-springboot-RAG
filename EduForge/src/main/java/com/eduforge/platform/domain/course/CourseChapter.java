package com.eduforge.platform.domain.course;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Chapitre d'un cours avec support PDF/PPTX/Vidéo/Texte
 */
@Entity
@Table(name = "course_chapter", indexes = {
    @Index(name = "idx_chap_course", columnList = "course_id")
})
public class CourseChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "chapter_order", nullable = false)
    private Integer chapterOrder;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    // Type de contenu: pdf, pptx, video, text
    @Column(name = "content_type", length = 20)
    private String contentType;

    // Chemin vers le fichier (PDF/PPTX) si uploadé
    @Column(name = "content_path")
    private String contentPath;

    // Contenu texte direct
    @Column(name = "text_content", columnDefinition = "text")
    private String textContent;

    // URL de vidéo externe (YouTube, etc.)
    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public CourseChapter() {}

    public CourseChapter(Long courseId, String title, Integer chapterOrder) {
        this.courseId = courseId;
        this.title = title;
        this.chapterOrder = chapterOrder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Integer getChapterOrder() { return chapterOrder; }
    public void setChapterOrder(Integer chapterOrder) { this.chapterOrder = chapterOrder; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getContentPath() { return contentPath; }
    public void setContentPath(String contentPath) { this.contentPath = contentPath; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean hasContent() {
        return contentPath != null || textContent != null || videoUrl != null;
    }

    public boolean isPdf() {
        return "pdf".equalsIgnoreCase(contentType);
    }

    public boolean isPptx() {
        return "pptx".equalsIgnoreCase(contentType);
    }

    public boolean isVideo() {
        return "video".equalsIgnoreCase(contentType);
    }

    public boolean isText() {
        return "text".equalsIgnoreCase(contentType);
    }
}

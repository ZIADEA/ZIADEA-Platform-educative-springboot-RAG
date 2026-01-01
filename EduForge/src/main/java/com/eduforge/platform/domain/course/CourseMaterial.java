package com.eduforge.platform.domain.course;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "course_material")
public class CourseMaterial {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false, length = 20)
    private String type; // PDF, PPTX, TEXT

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath; // path local

    @Column(name = "content_text", columnDefinition = "text")
    private String contentText; // texte extrait

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public CourseMaterial() {}

    public CourseMaterial(Long courseId, String type, String originalName, String storedPath, String contentText) {
        this.courseId = courseId;
        this.type = type;
        this.originalName = originalName;
        this.storedPath = storedPath;
        this.contentText = contentText;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

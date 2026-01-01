package com.eduforge.platform.domain.course;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(name = "course")
public class Course {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_prof_id", nullable = false)
    private Long ownerProfId;

    @NotBlank
    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 800)
    private String description;

    @Column(name = "text_content", columnDefinition = "text")
    private String textContent; // contenu direct (si prof colle du texte)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status = CourseStatus.DRAFT;

    // ID de la classe cible si status = CLASSE
    @Column(name = "target_classroom_id")
    private Long targetClassroomId;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Course() {}

    public Course(Long ownerProfId, String title, String description, String textContent) {
        this.ownerProfId = ownerProfId;
        this.title = title;
        this.description = description;
        this.textContent = textContent;
        this.status = CourseStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getOwnerProfId() { return ownerProfId; }
    public void setOwnerProfId(Long ownerProfId) { this.ownerProfId = ownerProfId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public Long getTargetClassroomId() { return targetClassroomId; }
    public void setTargetClassroomId(Long targetClassroomId) { this.targetClassroomId = targetClassroomId; }

    public Instant getIndexedAt() { return indexedAt; }
    public void setIndexedAt(Instant indexedAt) { this.indexedAt = indexedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    // Méthodes utilitaires
    public boolean isPublic() { return status == CourseStatus.PUBLIC; }
    public boolean isForClassroom() { return status == CourseStatus.CLASSE && targetClassroomId != null; }
    public boolean isDraft() { return status == CourseStatus.DRAFT; }
}

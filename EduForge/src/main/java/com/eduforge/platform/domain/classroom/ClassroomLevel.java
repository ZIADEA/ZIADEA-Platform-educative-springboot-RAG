package com.eduforge.platform.domain.classroom;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Niveau dans une classe (pour la progression)
 */
@Entity
@Table(name = "classroom_level", indexes = {
    @Index(name = "idx_cl_classroom", columnList = "classroom_id")
})
public class ClassroomLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "level_number", nullable = false)
    private Integer levelNumber = 1;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "required_score")
    private Integer requiredScore = 70;

    @Column(name = "unlock_threshold")
    private Integer unlockThreshold = 80;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public ClassroomLevel() {}

    public ClassroomLevel(Long classroomId, Integer levelNumber, String name) {
        this.classroomId = classroomId;
        this.levelNumber = levelNumber;
        this.name = name;
        this.createdAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Integer getLevelNumber() { return levelNumber; }
    public void setLevelNumber(Integer levelNumber) { this.levelNumber = levelNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRequiredScore() { return requiredScore; }
    public void setRequiredScore(Integer requiredScore) { this.requiredScore = requiredScore; }

    public Integer getUnlockThreshold() { return unlockThreshold; }
    public void setUnlockThreshold(Integer unlockThreshold) { this.unlockThreshold = unlockThreshold; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

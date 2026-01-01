package com.eduforge.platform.domain.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "subject")
public class Subject {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String name;

    public Subject() {}

    public Subject(Long levelId, String name) {
        this.levelId = levelId;
        this.name = name;
    }

    public Long getId() { return id; }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

package com.eduforge.platform.domain.classroom;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "classroom_post")
public class ClassroomPost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 20)
    private String type = "ANNOUNCEMENT"; // ANNOUNCEMENT / RESOURCE

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public ClassroomPost() {}

    public ClassroomPost(Long classroomId, Long authorId, String type, String content) {
        this.classroomId = classroomId;
        this.authorId = authorId;
        this.type = type;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

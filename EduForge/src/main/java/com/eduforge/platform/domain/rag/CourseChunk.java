package com.eduforge.platform.domain.rag;

import com.eduforge.platform.config.PgvectorType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;

@Entity
@Table(name = "course_chunk", indexes = {
        @Index(name = "idx_chunk_course", columnList = "course_id")
})
public class CourseChunk {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "chunk_text", columnDefinition = "text", nullable = false)
    private String chunkText;

    @Column(name = "terms_json", columnDefinition = "text", nullable = false)
    private String termsJson; // map(term -> tf) JSON

    // Embedding vectoriel pour la recherche sémantique (768 dimensions pour Gemini)
    // Utilise un type personnalisé pour la conversion float[] <-> pgvector
    @Column(name = "embedding", columnDefinition = "vector(768)")
    @Type(PgvectorType.class)
    private float[] embedding;

    @Column(name = "has_embedding")
    private boolean hasEmbedding = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public CourseChunk() {}

    public CourseChunk(Long courseId, int chunkIndex, String chunkText, String termsJson) {
        this.courseId = courseId;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
        this.termsJson = termsJson;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

    public String getTermsJson() { return termsJson; }
    public void setTermsJson(String termsJson) { this.termsJson = termsJson; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { 
        this.embedding = embedding; 
        this.hasEmbedding = (embedding != null);
    }

    public boolean isHasEmbedding() { return hasEmbedding; }
    public void setHasEmbedding(boolean hasEmbedding) { this.hasEmbedding = hasEmbedding; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

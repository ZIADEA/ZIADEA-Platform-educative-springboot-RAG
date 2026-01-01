package com.eduforge.platform.repository;

import com.eduforge.platform.domain.rag.CourseChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseChunkRepository extends JpaRepository<CourseChunk, Long> {
    List<CourseChunk> findByCourseIdOrderByChunkIndexAsc(Long courseId);
    void deleteByCourseId(Long courseId);
    long countByCourseId(Long courseId);

    // Recherche vectorielle via pgvector - retourne les chunks les plus similaires
    @Query(value = """
        SELECT c.* FROM course_chunk c 
        WHERE c.course_id = :courseId 
        AND c.has_embedding = true
        ORDER BY c.embedding <=> cast(:queryEmbedding as vector) 
        LIMIT :topK
        """, nativeQuery = true)
    List<CourseChunk> findSimilarChunks(
            @Param("courseId") Long courseId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") int topK);

    // Compter les chunks avec embedding
    long countByCourseIdAndHasEmbeddingTrue(Long courseId);

    // Chunks sans embedding (pour indexation en batch)
    List<CourseChunk> findByCourseIdAndHasEmbeddingFalse(Long courseId);
}

package com.eduforge.platform.repository;

import com.eduforge.platform.domain.course.CourseChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {
    
    List<CourseChapter> findByCourseIdOrderByChapterOrderAsc(Long courseId);
    
    List<CourseChapter> findByCourseIdAndIsPublishedTrueOrderByChapterOrderAsc(Long courseId);
    
    Optional<CourseChapter> findByCourseIdAndChapterOrder(Long courseId, Integer chapterOrder);
    
    long countByCourseId(Long courseId);
    
    @Query("SELECT MAX(c.chapterOrder) FROM CourseChapter c WHERE c.courseId = :courseId")
    Optional<Integer> getMaxOrderByCourse(Long courseId);
    
    void deleteByCourseId(Long courseId);
}

package com.eduforge.platform.repository;

import com.eduforge.platform.domain.course.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    
    List<CourseEnrollment> findByStudentIdOrderByEnrolledAtDesc(Long studentId);
    
    List<CourseEnrollment> findByCourseIdOrderByEnrolledAtDesc(Long courseId);
    
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);
    
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    
    long countByCourseId(Long courseId);
    
    long countByCourseIdAndIsCompletedTrue(Long courseId);
    
    @Modifying
    @Query("DELETE FROM CourseEnrollment e WHERE e.courseId = :courseId AND e.studentId = :studentId")
    void deleteByCourseIdAndStudentId(Long courseId, Long studentId);
    
    @Query("SELECT AVG(e.progressPercent) FROM CourseEnrollment e WHERE e.courseId = :courseId")
    Double averageProgressByCourseId(Long courseId);
    
    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.courseId = :courseId AND e.lastAccessedAt > :since")
    long countActiveSince(Long courseId, Instant since);
}

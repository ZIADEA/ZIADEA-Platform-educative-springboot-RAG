package com.eduforge.platform.repository;

import com.eduforge.platform.domain.exam.Exam;
import com.eduforge.platform.domain.exam.ExamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    List<Exam> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    List<Exam> findByClassroomIdAndStatusOrderByCreatedAtDesc(Long classroomId, ExamStatus status);
    
    List<Exam> findByCreatedByOrderByCreatedAtDesc(Long profId);
    
    @Query("SELECT e FROM Exam e WHERE e.classroomId = :classroomId " +
           "AND e.status IN ('PUBLISHED', 'SCHEDULED') " +
           "AND (e.isScheduled = false OR (e.scheduledStart <= :now AND (e.scheduledEnd IS NULL OR e.scheduledEnd >= :now)))")
    List<Exam> findAvailableExams(Long classroomId, Instant now);
    
    long countByClassroomId(Long classroomId);
    
    long countByClassroomIdAndStatus(Long classroomId, ExamStatus status);
    
    long countByCreatedBy(Long profId);
}

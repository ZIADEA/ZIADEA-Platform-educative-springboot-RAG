package com.eduforge.platform.repository;

import com.eduforge.platform.domain.exam.ExamAttempt;
import com.eduforge.platform.domain.exam.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    
    List<ExamAttempt> findByExamIdOrderByStartedAtDesc(Long examId);
    
    List<ExamAttempt> findByStudentIdOrderByStartedAtDesc(Long studentId);
    
    List<ExamAttempt> findByStudentIdOrderBySubmittedAtDesc(Long studentId);
    
    long countByStudentId(Long studentId);
    
    List<ExamAttempt> findByExamIdAndStudentIdOrderByStartedAtDesc(Long examId, Long studentId);
    
    Optional<ExamAttempt> findByExamIdAndStudentIdAndStatus(Long examId, Long studentId, AttemptStatus status);
    
    long countByExamIdAndStudentId(Long examId, Long studentId);
    
    @Query("SELECT AVG(a.scorePercent) FROM ExamAttempt a WHERE a.examId = :examId AND a.status = 'GRADED'")
    Double averageScoreByExamId(Long examId);
    
    @Query("SELECT COUNT(a) FROM ExamAttempt a WHERE a.examId = :examId AND a.scorePercent >= :threshold AND a.status = 'GRADED'")
    long countPassedByExamId(Long examId, int threshold);
}

package com.eduforge.platform.repository;

import com.eduforge.platform.domain.exam.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    @Query("SELECT q FROM ExamQuestion q WHERE q.examId = :examId ORDER BY q.qIndex ASC")
    List<ExamQuestion> findByExamIdOrderByQIndexAsc(@Param("examId") Long examId);
    
    void deleteByExamId(Long examId);
    
    long countByExamId(Long examId);
}

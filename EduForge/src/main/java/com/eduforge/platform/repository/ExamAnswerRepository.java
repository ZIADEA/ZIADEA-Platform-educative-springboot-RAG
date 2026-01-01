package com.eduforge.platform.repository;

import com.eduforge.platform.domain.exam.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, Long> {
    
    List<ExamAnswer> findByAttemptIdOrderByQuestionIdAsc(Long attemptId);
    
    void deleteByAttemptId(Long attemptId);
    
    long countByAttemptIdAndIsCorrectTrue(Long attemptId);
}

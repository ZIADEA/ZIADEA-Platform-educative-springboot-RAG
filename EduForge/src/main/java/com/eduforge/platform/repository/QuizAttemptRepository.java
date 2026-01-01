package com.eduforge.platform.repository;

import com.eduforge.platform.domain.quiz.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findTop20ByStudentIdAndCourseIdOrderByCreatedAtDesc(Long studentId, Long courseId);
    List<QuizAttempt> findTop50ByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<QuizAttempt> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    long countByCourseId(Long courseId);
    long countByStudentId(Long studentId);
}

package com.eduforge.platform.repository;

import com.eduforge.platform.domain.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseIdOrderByCreatedAtDesc(Long courseId);
}

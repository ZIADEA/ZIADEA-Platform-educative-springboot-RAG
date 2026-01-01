package com.eduforge.platform.repository;

import com.eduforge.platform.domain.quiz.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, Long> {
    List<QuizAttemptAnswer> findByAttemptId(Long attemptId);
}

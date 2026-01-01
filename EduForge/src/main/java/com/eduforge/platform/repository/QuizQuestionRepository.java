package com.eduforge.platform.repository;

import com.eduforge.platform.domain.quiz.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderByIndexAsc(Long quizId);
}

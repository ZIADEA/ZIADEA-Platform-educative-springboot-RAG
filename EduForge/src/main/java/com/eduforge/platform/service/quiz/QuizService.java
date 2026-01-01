package com.eduforge.platform.service.quiz;

import com.eduforge.platform.config.QuizProperties;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.quiz.*;
import com.eduforge.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuizService {

    public record QuizView(Quiz quiz, List<QuizQuestion> questions) {}
    public record AttemptView(QuizAttempt attempt, List<QuizQuestion> questions, Map<Long, QuizAttemptAnswer> answersByQ) {}

    private final QuizProperties quizProps;
    private final CourseRepository courses;
    private final QuizRepository quizRepo;
    private final QuizQuestionRepository questionRepo;
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository ansRepo;
    private final QuizAgentService agent;

    public QuizService(QuizProperties quizProps,
                       CourseRepository courses,
                       QuizRepository quizRepo,
                       QuizQuestionRepository questionRepo,
                       QuizAttemptRepository attemptRepo,
                       QuizAttemptAnswerRepository ansRepo,
                       QuizAgentService agent) {
        this.quizProps = quizProps;
        this.courses = courses;
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.attemptRepo = attemptRepo;
        this.ansRepo = ansRepo;
        this.agent = agent;
    }

    public int passThresholdFor(Course c) {
        // si la colonne existe (V3), sinon fallback
        try {
            var m = c.getClass().getMethod("getPassThreshold");
            Object v = m.invoke(c);
            if (v instanceof Integer i && i > 0) return i;
        } catch (Exception ignored) {}
        return quizProps.getDefaultPassThreshold();
    }

    @Transactional
    public QuizView generateAndPersist(Long courseId, Long studentId, String query) {
        return generateAndPersist(courseId, studentId, query, null, 0);
    }
    
    @Transactional
    public QuizView generateAndPersist(Long courseId, Long studentId, String query, Integer requestedCount, int openEndedCount) {
        Course c = courses.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
        Difficulty diff = agent.decideDifficulty(studentId, courseId);
        
        // Use requested count if provided, otherwise use adaptive count
        int count;
        if (requestedCount != null && requestedCount >= 3 && requestedCount <= 20) {
            count = requestedCount;
        } else {
            count = agent.decideQuestionCount(diff);
        }
        
        // Handle mixed quiz (MCQ + open-ended)
        if (openEndedCount > 0 && openEndedCount < count) {
            int mcqCount = count - openEndedCount;
            return generateMixedQuiz(courseId, studentId, query, mcqCount, openEndedCount, diff);
        }

        List<QuizAgentService.GeneratedQuestion> gqs = agent.generateQuiz(courseId, studentId, query, count);

        Quiz quiz = quizRepo.save(new Quiz(courseId, diff, count));

        List<QuizQuestion> saved = new ArrayList<>();
        int i = 1;
        for (var g : gqs) {
            QuizQuestion qq = new QuizQuestion(
                    quiz.getId(), i++,
                    g.question(),
                    g.choices().get("A"),
                    g.choices().get("B"),
                    g.choices().get("C"),
                    g.choices().get("D"),
                    g.correct(),
                    g.explanation()
            );
            saved.add(questionRepo.save(qq));
        }
        return new QuizView(quiz, saved);
    }
    
    @Transactional
    private QuizView generateMixedQuiz(Long courseId, Long studentId, String query, int mcqCount, int openEndedCount, Difficulty diff) {
        var unified = agent.generateMixedQuiz(courseId, studentId, query, mcqCount, openEndedCount);
        
        Quiz quiz = quizRepo.save(new Quiz(courseId, diff, mcqCount + openEndedCount));
        
        List<QuizQuestion> saved = new ArrayList<>();
        int i = 1;
        for (var u : unified) {
            QuizQuestion qq;
            if ("MCQ".equals(u.type())) {
                qq = new QuizQuestion(
                        quiz.getId(), i++,
                        u.question(),
                        u.choices().get("A"),
                        u.choices().get("B"),
                        u.choices().get("C"),
                        u.choices().get("D"),
                        u.correct(),
                        u.explanation()
                );
            } else {
                // Open-ended question
                qq = QuizQuestion.openEnded(
                        quiz.getId(), i++,
                        u.question(),
                        u.expectedAnswer(),
                        u.gradingCriteria(),
                        u.maxPoints() != null ? u.maxPoints() : 10,
                        u.explanation()
                );
            }
            saved.add(questionRepo.save(qq));
        }
        return new QuizView(quiz, saved);
    }

    public QuizView loadQuiz(Long quizId) {
        Quiz q = quizRepo.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz introuvable."));
        List<QuizQuestion> qs = questionRepo.findByQuizIdOrderByIndexAsc(quizId);
        return new QuizView(q, qs);
    }

    @Transactional
    public AttemptView submit(Long quizId, Long courseId, Long studentId, Map<Long, String> chosenByQuestionId) {
        QuizView qv = loadQuiz(quizId);
        if (!Objects.equals(qv.quiz().getCourseId(), courseId)) {
            throw new IllegalArgumentException("Quiz/course incohérents.");
        }

        List<QuizQuestion> qs = qv.questions();
        int total = qs.size();
        int correct = 0;

        QuizAttempt attempt = new QuizAttempt(quizId, courseId, studentId, 0, 0, total, AttemptStatus.FAILED);
        attempt = attemptRepo.save(attempt);

        Map<Long, QuizAttemptAnswer> ansMap = new HashMap<>();
        for (QuizQuestion q : qs) {
            String chosen = chosenByQuestionId.getOrDefault(q.getId(), "").toUpperCase(Locale.ROOT);
            if (!Set.of("A","B","C","D").contains(chosen)) chosen = "A"; // safe default
            boolean ok = chosen.equalsIgnoreCase(q.getCorrectChoice());
            if (ok) correct++;

            QuizAttemptAnswer a = ansRepo.save(new QuizAttemptAnswer(attempt.getId(), q.getId(), chosen, ok));
            ansMap.put(q.getId(), a);
        }

        int score = (int)Math.round((correct * 100.0) / Math.max(total, 1));

        Course course = courses.findById(courseId).orElseThrow();
        int threshold = passThresholdFor(course);
        AttemptStatus st = (score >= threshold) ? AttemptStatus.PASSED : AttemptStatus.FAILED;

        attempt.setCorrectCount(correct);
        attempt.setScorePercent(score);
        attempt.setStatus(st);

        return new AttemptView(attempt, qs, ansMap);
    }

    public List<QuizAttempt> studentHistory(Long studentId, Long courseId) {
        return attemptRepo.findTop20ByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
    }

    public List<QuizAttempt> courseRecentAttempts(Long courseId) {
        return attemptRepo.findTop50ByCourseIdOrderByCreatedAtDesc(courseId);
    }

    /**
     * Régénère un quiz plus facile après un échec
     * Cible les erreurs de l'étudiant et ajuste la difficulté
     * 
     * @param previousAttemptId L'ID de la tentative échouée
     * @param studentId L'ID de l'étudiant
     * @return Un nouveau quiz adaptatif
     */
    @Transactional
    public QuizView regenerateAdaptive(Long previousAttemptId, Long studentId) {
        QuizAttempt previousAttempt = attemptRepo.findById(previousAttemptId)
                .orElseThrow(() -> new IllegalArgumentException("Tentative introuvable."));

        if (!previousAttempt.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Cette tentative ne t'appartient pas.");
        }

        if (previousAttempt.getStatus() != AttemptStatus.FAILED) {
            throw new IllegalArgumentException("Ce quiz n'a pas échoué, pas besoin de régénération.");
        }

        Long courseId = previousAttempt.getCourseId();
        Long quizId = previousAttempt.getQuizId();

        // Identifier les questions échouées
        List<QuizAttemptAnswer> answers = ansRepo.findByAttemptId(previousAttemptId);
        List<Long> incorrectQuestionIds = new ArrayList<>();
        for (QuizAttemptAnswer ans : answers) {
            if (!ans.isCorrect()) {
                incorrectQuestionIds.add(ans.getQuestionId());
            }
        }

        if (incorrectQuestionIds.isEmpty()) {
            // Aucune erreur détectée, régénération standard
            return generateAndPersist(courseId, studentId, "quiz de rattrapage", null, 0);
        }

        // Récupérer les questions échouées
        List<QuizQuestion> incorrectQuestions = questionRepo.findAllById(incorrectQuestionIds);

        // Construire une requête ciblée sur les thèmes problématiques
        String focusQuery = incorrectQuestions.stream()
                .map(QuizQuestion::getQuestionText)
                .collect(java.util.stream.Collectors.joining(" "));

        // Générer un nouveau quiz EASIER ciblé sur ces thèmes
        Course c = courses.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
        
        // Force la difficulté à EASY pour la régénération
        List<QuizAgentService.GeneratedQuestion> gqs = agent.generateQuizWithDifficulty(
                courseId, studentId, focusQuery, incorrectQuestions.size() + 2, Difficulty.EASY);

        Quiz quiz = quizRepo.save(new Quiz(courseId, Difficulty.EASY, gqs.size()));

        List<QuizQuestion> saved = new ArrayList<>();
        int i = 1;
        for (var g : gqs) {
            QuizQuestion qq = new QuizQuestion(
                    quiz.getId(), i++,
                    g.question(),
                    g.choices().get("A"),
                    g.choices().get("B"),
                    g.choices().get("C"),
                    g.choices().get("D"),
                    g.correct(),
                    g.explanation()
            );
            saved.add(questionRepo.save(qq));
        }
        return new QuizView(quiz, saved);
    }
}

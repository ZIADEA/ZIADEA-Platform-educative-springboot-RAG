package com.eduforge.platform.service.exam;

import com.eduforge.platform.domain.exam.*;
import com.eduforge.platform.domain.quiz.QuestionType;
import com.eduforge.platform.repository.*;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.service.quiz.QuizAgentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ExamService {

    private final ExamRepository exams;
    private final ExamQuestionRepository questions;
    private final ExamAttemptRepository attempts;
    private final ExamAnswerRepository answers;
    private final CourseService courseService;
    private final QuizAgentService quizAgent;

    public ExamService(ExamRepository exams,
                       ExamQuestionRepository questions,
                       ExamAttemptRepository attempts,
                       ExamAnswerRepository answers,
                       CourseService courseService,
                       QuizAgentService quizAgent) {
        this.exams = exams;
        this.questions = questions;
        this.attempts = attempts;
        this.answers = answers;
        this.courseService = courseService;
        this.quizAgent = quizAgent;
    }

    public List<Exam> listByClassroom(Long classroomId) {
        return exams.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }

    public List<Exam> listAvailableExams(Long classroomId) {
        return exams.findAvailableExams(classroomId, Instant.now());
    }

    public List<Exam> listByProf(Long profId) {
        return exams.findByCreatedByOrderByCreatedAtDesc(profId);
    }

    public Optional<Exam> getById(Long examId) {
        return exams.findById(examId);
    }

    public Exam requireOwned(Long examId, Long profId) {
        return exams.findById(examId)
                .filter(e -> Objects.equals(e.getCreatedBy(), profId))
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable ou accès interdit."));
    }

    @Transactional
    public Exam create(Long classroomId, Long profId, String title, ExamType examType) {
        Exam exam = new Exam(classroomId, title, examType, profId);
        return exams.save(exam);
    }

    @Transactional
    public Exam update(Long examId, Long profId, String title, String description,
                       Integer questionCount, Integer durationMinutes, Integer passThreshold,
                       Boolean isTimed, Boolean shuffleQuestions, Boolean shuffleAnswers,
                       Boolean showScoreImmediately, Boolean allowReview, Integer maxAttempts) {
        Exam exam = requireOwned(examId, profId);
        
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalArgumentException("Impossible de modifier un examen déjà publié.");
        }

        exam.setTitle(title.trim());
        exam.setDescription(description);
        exam.setQuestionCount(questionCount);
        exam.setDurationMinutes(durationMinutes);
        exam.setPassThreshold(passThreshold);
        exam.setIsTimed(isTimed);
        exam.setShuffleQuestions(shuffleQuestions);
        exam.setShuffleAnswers(shuffleAnswers);
        exam.setShowScoreImmediately(showScoreImmediately);
        exam.setAllowReview(allowReview);
        exam.setMaxAttempts(maxAttempts);

        return exams.save(exam);
    }

    @Transactional
    public Exam schedule(Long examId, Long profId, Instant scheduledStart, Instant scheduledEnd) {
        Exam exam = requireOwned(examId, profId);
        
        exam.setIsScheduled(true);
        exam.setScheduledStart(scheduledStart);
        exam.setScheduledEnd(scheduledEnd);
        
        return exams.save(exam);
    }

    @Transactional
    public Exam generateQuestions(Long examId, Long profId, List<Long> courseIds) {
        Exam exam = requireOwned(examId, profId);
        
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalArgumentException("Impossible de regénérer les questions d'un examen publié.");
        }

        // Supprimer les anciennes questions
        questions.deleteByExamId(examId);

        // Générer via l'agent IA à partir de tous les cours sélectionnés
        List<QuizAgentService.GeneratedQuestion> allGenerated = new ArrayList<>();
        int questionsPerCourse = exam.getQuestionCount() / courseIds.size();
        int remainder = exam.getQuestionCount() % courseIds.size();
        
        for (int i = 0; i < courseIds.size(); i++) {
            Long courseId = courseIds.get(i);
            int count = questionsPerCourse + (i < remainder ? 1 : 0); // Distribuer le reste
            
            var generated = quizAgent.generateQuiz(courseId, profId, "examen complet", count);
            allGenerated.addAll(generated);
        }

        int idx = 0;
        for (var q : allGenerated) {
            ExamQuestion eq = new ExamQuestion(
                    examId, idx++,
                    q.question(),
                    q.choices().get("A"),
                    q.choices().get("B"),
                    q.choices().get("C"),
                    q.choices().get("D"),
                    q.correct(),
                    q.explanation()
            );
            questions.save(eq);
        }

        // Stocker le premier cours comme courseId principal (pour compatibilité)
        exam.setCourseId(courseIds.get(0));
        exam.setQuestionCount(allGenerated.size());
        return exams.save(exam);
    }

    @Transactional
    public Exam publish(Long examId, Long profId) {
        Exam exam = requireOwned(examId, profId);

        long qCount = questions.countByExamId(examId);
        if (qCount == 0) {
            throw new IllegalArgumentException("L'examen doit contenir des questions avant publication.");
        }

        exam.setStatus(ExamStatus.PUBLISHED);
        exam.setPublishedAt(Instant.now());
        return exams.save(exam);
    }

    @Transactional
    public Exam close(Long examId, Long profId) {
        Exam exam = requireOwned(examId, profId);
        exam.setStatus(ExamStatus.CLOSED);
        return exams.save(exam);
    }

    // --- Édition de questions avant publication ---

    @Transactional
    public ExamQuestion updateQuestion(Long questionId, Long profId, String questionType, String questionText,
                                       String choiceA, String choiceB, String choiceC, String choiceD,
                                       String correctChoice, String expectedAnswer, String gradingRubric, String explanation) {
        ExamQuestion question = questions.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question introuvable."));
        
        Exam exam = requireOwned(question.getExamId(), profId);
        
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalArgumentException("Impossible de modifier les questions d'un examen publié.");
        }

        question.setQuestionText(questionText.trim());
        question.setQuestionType(QuestionType.valueOf(questionType));
        
        if (question.getQuestionType() == QuestionType.MCQ) {
            question.setAText(choiceA != null ? choiceA.trim() : null);
            question.setBText(choiceB != null ? choiceB.trim() : null);
            question.setCText(choiceC != null ? choiceC.trim() : null);
            question.setDText(choiceD != null ? choiceD.trim() : null);
            question.setCorrectChoice(correctChoice != null ? correctChoice.toUpperCase() : null);
            // Effacer les champs open-ended
            question.setExpectedAnswer(null);
            question.setGradingRubric(null);
        } else {
            question.setExpectedAnswer(expectedAnswer != null ? expectedAnswer.trim() : null);
            question.setGradingRubric(gradingRubric != null ? gradingRubric.trim() : null);
            // Effacer les champs MCQ
            question.setAText(null);
            question.setBText(null);
            question.setCText(null);
            question.setDText(null);
            question.setCorrectChoice(null);
        }
        
        question.setExplanation(explanation);

        return questions.save(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long profId) {
        ExamQuestion question = questions.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question introuvable."));
        
        Exam exam = requireOwned(question.getExamId(), profId);
        
        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalArgumentException("Impossible de supprimer les questions d'un examen publié.");
        }

        questions.delete(question);
        
        // Réindexer les questions restantes
        List<ExamQuestion> remaining = questions.findByExamIdOrderByQIndexAsc(exam.getId());
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setQIndex(i);
            questions.save(remaining.get(i));
        }
        
        // Mettre à jour le nombre de questions de l'examen
        exam.setQuestionCount(remaining.size());
        exams.save(exam);
    }

    // --- Tentatives étudiants ---

    public List<ExamQuestion> getQuestions(Long examId) {
        return questions.findByExamIdOrderByQIndexAsc(examId);
    }

    public boolean canStudentAttempt(Long examId, Long studentId) {
        Exam exam = exams.findById(examId).orElse(null);
        if (exam == null) return false;
        
        if (!exam.isAvailable()) return false;
        
        long attemptCount = attempts.countByExamIdAndStudentId(examId, studentId);
        return attemptCount < exam.getMaxAttempts();
    }

    @Transactional
    public ExamAttempt startAttempt(Long examId, Long studentId) {
        Exam exam = exams.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable."));

        if (!exam.isAvailable()) {
            throw new IllegalArgumentException("Cet examen n'est pas disponible.");
        }

        long attemptCount = attempts.countByExamIdAndStudentId(examId, studentId);
        if (attemptCount >= exam.getMaxAttempts()) {
            throw new IllegalArgumentException("Nombre maximum de tentatives atteint.");
        }

        // Vérifier s'il y a une tentative en cours
        var inProgress = attempts.findByExamIdAndStudentIdAndStatus(
                examId, studentId, AttemptStatus.IN_PROGRESS);
        if (inProgress.isPresent()) {
            return inProgress.get();
        }

        ExamAttempt attempt = new ExamAttempt(examId, studentId, (int) attemptCount + 1);
        return attempts.save(attempt);
    }

    @Transactional
    public ExamAttempt submitAttempt(Long attemptId, Map<Long, String> mcqAnswers, Map<Long, String> textAnswers) {
        ExamAttempt attempt = attempts.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Tentative introuvable."));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Cette tentative a déjà été soumise.");
        }

        List<ExamQuestion> examQuestions = questions.findByExamIdOrderByQIndexAsc(attempt.getExamId());
        
        int correct = 0;
        int total = examQuestions.size();
        double totalScore = 0.0; // Pour questions ouvertes avec scores partiels

        for (ExamQuestion q : examQuestions) {
            ExamAnswer answer;
            
            if (q.getQuestionType() == QuestionType.MCQ) {
                // Traitement QCM
                String chosen = mcqAnswers.get(q.getId());
                boolean isCorrect = chosen != null && chosen.equalsIgnoreCase(q.getCorrectChoice());
                
                if (isCorrect) {
                    correct++;
                    totalScore += 100;
                }

                answer = new ExamAnswer(attemptId, q.getId(), chosen, isCorrect);
                answers.save(answer);
                
            } else {
                // Traitement question ouverte
                String textAnswer = textAnswers.get(q.getId());
                answer = new ExamAnswer(attemptId, q.getId(), textAnswer);
                
                // Grade avec AI de manière asynchrone ou synchrone selon le besoin
                if (textAnswer != null && !textAnswer.trim().isEmpty()) {
                    try {
                        var grading = quizAgent.gradeOpenEndedAnswer(
                            q.getQuestionText(), 
                            q.getExpectedAnswer(), 
                            q.getGradingRubric(),
                            textAnswer
                        );
                        answer.setAiScore(grading.score());
                        answer.setAiFeedback(grading.feedback());
                        totalScore += grading.score();
                        
                        // Considérer comme correct si score >= 70%
                        if (grading.score() >= 70) {
                            correct++;
                            answer.setIsCorrect(true);
                        } else {
                            answer.setIsCorrect(false);
                        }
                    } catch (Exception e) {
                        // En cas d'erreur de grading AI, on met un score neutre
                        answer.setAiScore(50);
                        answer.setAiFeedback("Erreur lors de l'évaluation automatique. Un correcteur humain vérifiera votre réponse.");
                        totalScore += 50;
                    }
                } else {
                    answer.setAiScore(0);
                    answer.setAiFeedback("Aucune réponse fournie.");
                    answer.setIsCorrect(false);
                }
                
                answers.save(answer);
            }
        }

        // Calculer le score final
        int scorePercent = total > 0 ? (int) (totalScore / total) : 0;
        
        attempt.setCorrectCount(correct);
        attempt.setTotalCount(total);
        attempt.setScorePercent(scorePercent);
        attempt.setStatus(AttemptStatus.GRADED);
        attempt.setSubmittedAt(Instant.now());

        // Calculer le temps passé
        long seconds = java.time.Duration.between(attempt.getStartedAt(), Instant.now()).getSeconds();
        attempt.setTimeSpentSeconds((int) seconds);

        return attempts.save(attempt);
    }

    public List<ExamAttempt> getStudentAttempts(Long examId, Long studentId) {
        return attempts.findByExamIdAndStudentIdOrderByStartedAtDesc(examId, studentId);
    }

    public List<ExamAttempt> getAllAttempts(Long examId) {
        return attempts.findByExamIdOrderByStartedAtDesc(examId);
    }

    public List<ExamAnswer> getAttemptAnswers(Long attemptId) {
        return answers.findByAttemptIdOrderByQuestionIdAsc(attemptId);
    }

    // --- Statistiques ---

    public record ExamStats(long totalAttempts, double averageScore, long passedCount, double passRate) {}

    public ExamStats getStats(Long examId) {
        Exam exam = exams.findById(examId).orElse(null);
        if (exam == null) return new ExamStats(0, 0, 0, 0);

        List<ExamAttempt> allAttempts = attempts.findByExamIdOrderByStartedAtDesc(examId);
        long total = allAttempts.stream().filter(a -> a.getStatus() == AttemptStatus.GRADED).count();
        
        if (total == 0) return new ExamStats(0, 0, 0, 0);

        Double avg = attempts.averageScoreByExamId(examId);
        long passed = attempts.countPassedByExamId(examId, exam.getPassThreshold());
        double passRate = (passed * 100.0) / total;

        return new ExamStats(total, avg != null ? avg : 0, passed, passRate);
    }

    @Transactional
    public void delete(Long examId, Long profId) {
        Exam exam = requireOwned(examId, profId);
        
        // Supprimer les réponses, tentatives, questions
        for (ExamAttempt a : attempts.findByExamIdOrderByStartedAtDesc(examId)) {
            answers.deleteByAttemptId(a.getId());
        }
        attempts.deleteAll(attempts.findByExamIdOrderByStartedAtDesc(examId));
        questions.deleteByExamId(examId);
        exams.delete(exam);
    }
}

package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.exam.*;
import com.eduforge.platform.service.exam.ExamService;
import com.eduforge.platform.service.classroom.StudentProgressService;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/exam")
public class StudentExamController {

    private final ExamService examService;
    private final StudentProgressService progressService;

    public StudentExamController(ExamService examService,
                                 StudentProgressService progressService) {
        this.examService = examService;
        this.progressService = progressService;
    }

    @GetMapping("/{examId}")
    public String view(Authentication auth,
                       @PathVariable Long examId,
                       Model model) {
        Long studentId = SecurityUtil.userId(auth);
        
        Exam exam = examService.getById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable."));

        var attempts = examService.getStudentAttempts(examId, studentId);
        boolean canAttempt = examService.canStudentAttempt(examId, studentId);

        model.addAttribute("pageTitle", exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("attempts", attempts);
        model.addAttribute("canAttempt", canAttempt);
        return "student/exam_view";
    }

    @GetMapping("/{examId}/start")
    public String start(Authentication auth,
                        @PathVariable Long examId,
                        Model model) {
        Long studentId = SecurityUtil.userId(auth);
        
        Exam exam = examService.getById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable."));

        if (!examService.canStudentAttempt(examId, studentId)) {
            return "redirect:/student/exam/" + examId + "?cantAttempt";
        }

        ExamAttempt attempt = examService.startAttempt(examId, studentId);
        List<ExamQuestion> questions = examService.getQuestions(examId);

        // Mélanger si nécessaire
        if (exam.getShuffleQuestions()) {
            Collections.shuffle(questions);
        }

        model.addAttribute("pageTitle", exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        return "student/exam_take";
    }

    @PostMapping("/{examId}/submit")
    public String submit(Authentication auth,
                         @PathVariable Long examId,
                         @RequestParam Long attemptId,
                         @RequestParam Map<String, String> allParams) {
        Long studentId = SecurityUtil.userId(auth);
        
        // Extraire les réponses MCQ (format: answer_questionId=choice)
        Map<Long, String> mcqAnswers = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("answer_"))
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().replace("answer_", "")),
                        Map.Entry::getValue
                ));

        // Extraire les réponses textuelles (format: text_answer_questionId=text)
        Map<Long, String> textAnswers = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("text_answer_"))
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().replace("text_answer_", "")),
                        Map.Entry::getValue
                ));

        ExamAttempt attempt = examService.submitAttempt(attemptId, mcqAnswers, textAnswers);

        // Mettre à jour la progression
        Exam exam = examService.getById(examId).orElse(null);
        if (exam != null) {
            boolean passed = attempt.getScorePercent() >= exam.getPassThreshold();
            progressService.recordExamAttempt(exam.getClassroomId(), studentId, 
                    attempt.getScorePercent(), passed);
        }

        // Rediriger vers les résultats
        if (exam != null && exam.getShowScoreImmediately()) {
            return "redirect:/student/exam/" + examId + "/result/" + attemptId;
        }
        return "redirect:/student/exam/" + examId + "?submitted";
    }

    @GetMapping("/{examId}/result/{attemptId}")
    public String result(Authentication auth,
                         @PathVariable Long examId,
                         @PathVariable Long attemptId,
                         Model model) {
        Long studentId = SecurityUtil.userId(auth);
        
        Exam exam = examService.getById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Examen introuvable."));

        var attempts = examService.getStudentAttempts(examId, studentId);
        ExamAttempt attempt = attempts.stream()
                .filter(a -> a.getId().equals(attemptId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tentative introuvable."));

        List<ExamAnswer> answersList = examService.getAttemptAnswers(attemptId);
        List<ExamQuestion> questions = examService.getQuestions(examId);

        // Créer un map questionId -> answer
        Map<Long, ExamAnswer> answersMap = answersList.stream()
                .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));

        model.addAttribute("pageTitle", "Résultat: " + exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("answersMap", answersMap);
        model.addAttribute("allowReview", exam.getAllowReview());
        return "student/exam_result";
    }
}

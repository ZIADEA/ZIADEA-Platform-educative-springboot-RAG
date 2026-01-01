package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.exam.*;
import com.eduforge.platform.service.exam.ExamService;
import com.eduforge.platform.service.classroom.ClassroomService;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.service.messaging.NotificationService;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@Controller
@RequestMapping("/prof/exam")
public class ProfExamController {

    private final ExamService examService;
    private final ClassroomService classroomService;
    private final CourseService courseService;
    private final NotificationService notifications;

    public ProfExamController(ExamService examService,
                              ClassroomService classroomService,
                              CourseService courseService,
                              NotificationService notifications) {
        this.examService = examService;
        this.classroomService = classroomService;
        this.courseService = courseService;
        this.notifications = notifications;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        Long profId = SecurityUtil.userId(auth);
        var exams = examService.listByProf(profId);
        var classrooms = classroomService.profClasses(profId);

        model.addAttribute("pageTitle", "Mes examens");
        model.addAttribute("exams", exams);
        model.addAttribute("classrooms", classrooms);
        return "prof/exams";
    }

    @GetMapping("/create")
    public String createForm(Authentication auth, Model model) {
        Long profId = SecurityUtil.userId(auth);
        var classrooms = classroomService.profClasses(profId);

        model.addAttribute("pageTitle", "Créer un examen");
        model.addAttribute("classrooms", classrooms);
        model.addAttribute("examTypes", ExamType.values());
        return "prof/exam_create";
    }

    @PostMapping("/create")
    public String create(Authentication auth,
                         @RequestParam Long classroomId,
                         @RequestParam String title,
                         @RequestParam String examType) {
        Long profId = SecurityUtil.userId(auth);
        
        ExamType type = ExamType.valueOf(examType);
        Exam exam = examService.create(classroomId, profId, title, type);
        
        return "redirect:/prof/exam/" + exam.getId() + "/edit";
    }

    @GetMapping("/{examId}")
    public String view(Authentication auth,
                       @PathVariable Long examId,
                       Model model) {
        Long profId = SecurityUtil.userId(auth);
        Exam exam = examService.requireOwned(examId, profId);
        var questions = examService.getQuestions(examId);
        var stats = examService.getStats(examId);
        var attempts = examService.getAllAttempts(examId);

        model.addAttribute("pageTitle", exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("stats", stats);
        model.addAttribute("attempts", attempts);
        return "prof/exam_view";
    }

    @GetMapping("/{examId}/edit")
    public String editForm(Authentication auth,
                           @PathVariable Long examId,
                           Model model) {
        Long profId = SecurityUtil.userId(auth);
        Exam exam = examService.requireOwned(examId, profId);
        var questions = examService.getQuestions(examId);
        var profCourses = courseService.profCourses(profId);

        model.addAttribute("pageTitle", "Modifier: " + exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("questions", questions);
        model.addAttribute("profCourses", profCourses);
        return "prof/exam_edit";
    }

    @PostMapping("/{examId}/edit")
    public String edit(Authentication auth,
                       @PathVariable Long examId,
                       @RequestParam String title,
                       @RequestParam(required = false) String description,
                       @RequestParam(defaultValue = "10") Integer questionCount,
                       @RequestParam(defaultValue = "60") Integer durationMinutes,
                       @RequestParam(defaultValue = "50") Integer passThreshold,
                       @RequestParam(defaultValue = "true") Boolean isTimed,
                       @RequestParam(defaultValue = "false") Boolean shuffleQuestions,
                       @RequestParam(defaultValue = "false") Boolean shuffleAnswers,
                       @RequestParam(defaultValue = "true") Boolean showScoreImmediately,
                       @RequestParam(defaultValue = "true") Boolean allowReview,
                       @RequestParam(defaultValue = "1") Integer maxAttempts) {
        Long profId = SecurityUtil.userId(auth);
        
        examService.update(examId, profId, title, description, questionCount, 
                durationMinutes, passThreshold, isTimed, shuffleQuestions, shuffleAnswers,
                showScoreImmediately, allowReview, maxAttempts);
        
        return "redirect:/prof/exam/" + examId + "/edit";
    }

    @PostMapping("/{examId}/generate")
    public String generateQuestions(Authentication auth,
                                    @PathVariable Long examId,
                                    @RequestParam List<Long> courseIds) {
        Long profId = SecurityUtil.userId(auth);
        
        if (courseIds == null || courseIds.isEmpty()) {
            return "redirect:/prof/exam/" + examId + "/edit?error=Veuillez sélectionner au moins un cours";
        }
        
        try {
            examService.generateQuestions(examId, profId, courseIds);
            return "redirect:/prof/exam/" + examId + "/edit?generated";
        } catch (Exception e) {
            return "redirect:/prof/exam/" + examId + "/edit?error=" + e.getMessage();
        }
    }

    @PostMapping("/{examId}/schedule")
    public String schedule(Authentication auth,
                           @PathVariable Long examId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledStart,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledEnd) {
        Long profId = SecurityUtil.userId(auth);
        
        Instant start = scheduledStart.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = scheduledEnd.atZone(ZoneId.systemDefault()).toInstant();
        
        examService.schedule(examId, profId, start, end);
        
        return "redirect:/prof/exam/" + examId + "/edit?scheduled";
    }

    @PostMapping("/{examId}/publish")
    public String publish(Authentication auth,
                          @PathVariable Long examId) {
        Long profId = SecurityUtil.userId(auth);
        
        try {
            Exam exam = examService.publish(examId, profId);
            // Notifier les étudiants de la classe
            // TODO: obtenir la liste des étudiants et les notifier
            return "redirect:/prof/exam/" + examId + "?published";
        } catch (IllegalArgumentException e) {
            return "redirect:/prof/exam/" + examId + "/edit?error=" + e.getMessage();
        }
    }

    @PostMapping("/{examId}/close")
    public String close(Authentication auth,
                        @PathVariable Long examId) {
        Long profId = SecurityUtil.userId(auth);
        examService.close(examId, profId);
        return "redirect:/prof/exam/" + examId;
    }

    @PostMapping("/{examId}/delete")
    public String delete(Authentication auth,
                         @PathVariable Long examId) {
        Long profId = SecurityUtil.userId(auth);
        examService.delete(examId, profId);
        return "redirect:/prof/exam";
    }

    @PostMapping("/{examId}/question/{questionId}/edit")
    public String editQuestion(Authentication auth,
                               @PathVariable Long examId,
                               @PathVariable Long questionId,
                               @RequestParam String questionType,
                               @RequestParam String questionText,
                               @RequestParam(required = false) String choiceA,
                               @RequestParam(required = false) String choiceB,
                               @RequestParam(required = false) String choiceC,
                               @RequestParam(required = false) String choiceD,
                               @RequestParam(required = false) String correctChoice,
                               @RequestParam(required = false) String expectedAnswer,
                               @RequestParam(required = false) String gradingRubric,
                               @RequestParam(required = false) String explanation) {
        Long profId = SecurityUtil.userId(auth);
        
        try {
            examService.updateQuestion(questionId, profId, questionType, questionText, 
                    choiceA, choiceB, choiceC, choiceD, correctChoice, 
                    expectedAnswer, gradingRubric, explanation);
            return "redirect:/prof/exam/" + examId + "/edit?questionUpdated";
        } catch (Exception e) {
            return "redirect:/prof/exam/" + examId + "/edit?error=" + e.getMessage();
        }
    }

    @PostMapping("/{examId}/question/{questionId}/delete")
    public String deleteQuestion(Authentication auth,
                                 @PathVariable Long examId,
                                 @PathVariable Long questionId) {
        Long profId = SecurityUtil.userId(auth);
        
        try {
            examService.deleteQuestion(questionId, profId);
            return "redirect:/prof/exam/" + examId + "/edit?questionDeleted";
        } catch (Exception e) {
            return "redirect:/prof/exam/" + examId + "/edit?error=" + e.getMessage();
        }
    }

    @GetMapping("/{examId}/results")
    public String results(Authentication auth,
                          @PathVariable Long examId,
                          Model model) {
        Long profId = SecurityUtil.userId(auth);
        Exam exam = examService.requireOwned(examId, profId);
        var stats = examService.getStats(examId);
        var attempts = examService.getAllAttempts(examId);

        model.addAttribute("pageTitle", "Résultats: " + exam.getTitle());
        model.addAttribute("exam", exam);
        model.addAttribute("stats", stats);
        model.addAttribute("attempts", attempts);
        return "prof/exam_results";
    }
}

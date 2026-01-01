package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.service.quiz.QuizService;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.QuizStartForm;
import com.eduforge.platform.web.dto.forms.QuizSubmitForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/quiz")
public class StudentQuizController {

    private final CourseService courseService;
    private final QuizService quizService;

    public StudentQuizController(CourseService courseService, 
                                  QuizService quizService) {
        this.courseService = courseService;
        this.quizService = quizService;
    }

    @GetMapping("/start/{courseId}")
    public String startPage(Authentication auth, @PathVariable Long courseId, Model model) {
        Long studentId = SecurityUtil.userId(auth);

        if (!courseService.studentCanAccessCourse(studentId, courseId)) {
            throw new IllegalArgumentException("Accès refusé.");
        }

        Course c = courseService.getById(courseId);
        model.addAttribute("pageTitle", "Démarrer un quiz");
        model.addAttribute("course", c);
        model.addAttribute("form", new QuizStartForm());
        model.addAttribute("history", quizService.studentHistory(studentId, courseId));
        return "student/quiz_start";
    }

    @PostMapping("/start/{courseId}")
    public String start(Authentication auth,
                        @PathVariable Long courseId,
                        @Valid @ModelAttribute("form") QuizStartForm form) {
        Long studentId = SecurityUtil.userId(auth);

        if (!courseService.studentCanAccessCourse(studentId, courseId)) {
            throw new IllegalArgumentException("Accès refusé.");
        }

        int openEnded = form.isIncludeOpenEnded() && form.getOpenEndedCount() != null ? form.getOpenEndedCount() : 0;
        var qv = quizService.generateAndPersist(courseId, studentId, form.getQuery(), form.getQuestionCount(), openEnded);
        return "redirect:/student/quiz/take/" + qv.quiz().getId();
    }

    @GetMapping("/take/{quizId}")
    public String take(Authentication auth, @PathVariable Long quizId, Model model) {
        Long studentId = SecurityUtil.userId(auth);
        var qv = quizService.loadQuiz(quizId);

        if (!courseService.studentCanAccessCourse(studentId, qv.quiz().getCourseId())) {
            throw new IllegalArgumentException("Accès refusé.");
        }

        model.addAttribute("pageTitle", "Quiz");
        model.addAttribute("quiz", qv.quiz());
        model.addAttribute("questions", qv.questions());
        QuizSubmitForm submit = new QuizSubmitForm();
        submit.setQuizId(quizId);
        submit.setCourseId(qv.quiz().getCourseId());
        model.addAttribute("form", submit);
        return "student/quiz_take";
    }

    @PostMapping("/submit")
    public String submit(Authentication auth, @ModelAttribute("form") QuizSubmitForm form, Model model) {
        Long studentId = SecurityUtil.userId(auth);

        if (!courseService.studentCanAccessCourse(studentId, form.getCourseId())) {
            throw new IllegalArgumentException("Accès refusé.");
        }

        var av = quizService.submit(form.getQuizId(), form.getCourseId(), studentId, form.getAnswers());
        var course = courseService.getById(form.getCourseId());

        model.addAttribute("pageTitle", "Résultat");
        model.addAttribute("course", course);
        model.addAttribute("attempt", av.attempt());
        model.addAttribute("questions", av.questions());
        model.addAttribute("answersByQ", av.answersByQ());
        model.addAttribute("threshold", quizService.passThresholdFor(course));
        return "student/quiz_result";
    }

    /**
     * Régénère un quiz adaptatif après un échec
     */
    @PostMapping("/regenerate/{attemptId}")
    public String regenerateAdaptive(Authentication auth, 
                                      @PathVariable Long attemptId,
                                      RedirectAttributes ra) {
        Long studentId = SecurityUtil.userId(auth);

        try {
            var qv = quizService.regenerateAdaptive(attemptId, studentId);
            ra.addFlashAttribute("flashSuccess", "Quiz régénéré avec des questions plus faciles et ciblées sur tes difficultés !");
            return "redirect:/student/quiz/take/" + qv.quiz().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/student/courses";
        }
    }
}

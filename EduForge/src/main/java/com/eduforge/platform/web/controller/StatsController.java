package com.eduforge.platform.web.controller;

import com.eduforge.platform.repository.CourseRepository;
import com.eduforge.platform.repository.QuizAttemptRepository;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/stats")
public class StatsController {

    private final CourseRepository courses;
    private final QuizAttemptRepository attempts;
    private final CourseService courseService;

    public StatsController(CourseRepository courses, QuizAttemptRepository attempts, CourseService courseService) {
        this.courses = courses;
        this.attempts = attempts;
        this.courseService = courseService;
    }

    @GetMapping("/admin")
    public String admin(Authentication auth, Model model) {
        // Admin only (protégé par security)
        model.addAttribute("pageTitle", "Admin — Statistiques");
        model.addAttribute("courseCount", courses.count());
        model.addAttribute("attemptCount", attempts.count());
        return "stats/admin";
    }

    @GetMapping("/prof/course/{courseId}")
    public String profCourse(Authentication auth, @PathVariable Long courseId, Model model) {
        Long profId = SecurityUtil.userId(auth);
        var c = courseService.requireOwned(courseId, profId);
        var recents = attempts.findTop50ByCourseIdOrderByCreatedAtDesc(courseId);

        long total = attempts.countByCourseId(courseId);
        double avg = recents.stream().mapToInt(a -> a.getScorePercent()).average().orElse(0);

        model.addAttribute("pageTitle", "Prof — Stats cours");
        model.addAttribute("course", c);
        model.addAttribute("totalAttempts", total);
        model.addAttribute("avgRecent", avg);
        model.addAttribute("recents", recents);
        return "stats/prof_course";
    }
}


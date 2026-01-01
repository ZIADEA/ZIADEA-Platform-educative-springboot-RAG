package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.course.CourseStatus;
import com.eduforge.platform.repository.CourseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PublicController {

    private final CourseRepository courses;

    public PublicController(CourseRepository courses) {
        this.courses = courses;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "EduForge — Plateforme pédagogique IA");
        model.addAttribute("publishedCount", courses.countByStatus(CourseStatus.PUBLIC));
        return "public/index";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "q", required = false) String q, Model model) {
        model.addAttribute("pageTitle", "Catalogue des cours");

        List<Course> list;
        if (q != null && !q.isBlank()) {
            list = courses.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(CourseStatus.PUBLIC, q.trim());
        } else {
            list = courses.findByStatusOrderByCreatedAtDesc(CourseStatus.PUBLIC);
        }

        model.addAttribute("q", q);
        model.addAttribute("courses", list);
        model.addAttribute("coursesCount", list.size());
        return "public/catalog";
    }
}

package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.AccountStatus;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.course.CourseStatus;
import com.eduforge.platform.repository.ClassroomEnrollmentRepository;
import com.eduforge.platform.repository.ClassroomRepository;
import com.eduforge.platform.repository.CourseRepository;
import com.eduforge.platform.repository.QuizAttemptRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository users;
    private final CourseRepository courses;
    private final ClassroomRepository classrooms;
    private final ClassroomEnrollmentRepository enrollments;
    private final QuizAttemptRepository attempts;

    public AdminController(UserRepository users,
                           CourseRepository courses,
                           ClassroomRepository classrooms,
                           ClassroomEnrollmentRepository enrollments,
                           QuizAttemptRepository attempts) {
        this.users = users;
        this.courses = courses;
        this.classrooms = classrooms;
        this.enrollments = enrollments;
        this.attempts = attempts;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Admin — Dashboard");
        model.addAttribute("userCount", users.count());
        model.addAttribute("courseCount", courses.count());
        model.addAttribute("publishedCourseCount", courses.countByStatus(CourseStatus.PUBLISHED));
        model.addAttribute("classCount", classrooms.count());
        model.addAttribute("enrollmentCount", enrollments.count());
        model.addAttribute("attemptCount", attempts.count());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersList(@RequestParam(value = "q", required = false) String q,
                            Model model) {
        model.addAttribute("pageTitle", "Admin — Utilisateurs");
        List<User> all = users.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (q != null && !q.isBlank()) {
            String qq = q.trim().toLowerCase(Locale.ROOT);
            all = all.stream()
                    .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(qq))
                            || (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(qq))
                            || (u.getRole() != null && u.getRole().name().toLowerCase(Locale.ROOT).contains(qq))
                            || (u.getStatus() != null && u.getStatus().name().toLowerCase(Locale.ROOT).contains(qq)))
                    .collect(Collectors.toList());
        }
        model.addAttribute("q", q);
        model.addAttribute("users", all);
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    @Transactional
    public String toggleUser(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes ra) {
        Long me = SecurityUtil.userId(auth);
        if (Objects.equals(me, id)) {
            ra.addFlashAttribute("flashError", "Tu ne peux pas désactiver ton propre compte.");
            return "redirect:/admin/users";
        }
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        if (u.getStatus() == AccountStatus.DISABLED) {
            u.setStatus(AccountStatus.ACTIVE);
            ra.addFlashAttribute("flashSuccess", "Compte réactivé.");
        } else {
            u.setStatus(AccountStatus.DISABLED);
            ra.addFlashAttribute("flashSuccess", "Compte désactivé.");
        }
        users.save(u);
        return "redirect:/admin/users";
    }

    @GetMapping("/courses")
    public String coursesList(@RequestParam(value = "q", required = false) String q,
                              Model model) {
        model.addAttribute("pageTitle", "Admin — Cours");
        List<Course> all = courses.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (q != null && !q.isBlank()) {
            String qq = q.trim().toLowerCase(Locale.ROOT);
            all = all.stream()
                    .filter(c -> (c.getTitle() != null && c.getTitle().toLowerCase(Locale.ROOT).contains(qq))
                            || (c.getDescription() != null && c.getDescription().toLowerCase(Locale.ROOT).contains(qq))
                            || (c.getStatus() != null && c.getStatus().name().toLowerCase(Locale.ROOT).contains(qq)))
                    .collect(Collectors.toList());
        }

        Set<Long> ownerIds = all.stream().map(Course::getOwnerProfId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> owners = users.findAllById(ownerIds).stream().collect(Collectors.toMap(User::getId, x -> x));

        model.addAttribute("q", q);
        model.addAttribute("courses", all);
        model.addAttribute("owners", owners);
        return "admin/courses";
    }

    @PostMapping("/courses/{courseId}/unpublish")
    @Transactional
    public String unpublish(@PathVariable Long courseId, RedirectAttributes ra) {
        Course c = courses.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
        c.setStatus(CourseStatus.DRAFT);
        courses.save(c);
        ra.addFlashAttribute("flashSuccess", "Cours repassé en brouillon.");
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/{courseId}/delete")
    @Transactional
    public String deleteCourse(@PathVariable Long courseId, RedirectAttributes ra) {
        if (!courses.existsById(courseId)) {
            ra.addFlashAttribute("flashError", "Cours introuvable.");
            return "redirect:/admin/courses";
        }
        courses.deleteById(courseId);
        ra.addFlashAttribute("flashSuccess", "Cours supprimé.");
        return "redirect:/admin/courses";
    }
}

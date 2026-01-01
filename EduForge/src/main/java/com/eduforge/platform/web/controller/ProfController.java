package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.classroom.Classroom;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.repository.ClassroomEnrollmentRepository;
import com.eduforge.platform.repository.ClassroomRepository;
import com.eduforge.platform.repository.CourseRepository;
import com.eduforge.platform.repository.ExamRepository;
import com.eduforge.platform.service.classroom.ClassroomService;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.ClassroomCreateForm;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prof")
public class ProfController {

    private final ClassroomService classroomService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final ExamRepository examRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;

    public ProfController(ClassroomService classroomService,
                          CourseService courseService,
                          CourseRepository courseRepository,
                          ClassroomRepository classroomRepository,
                          ExamRepository examRepository,
                          ClassroomEnrollmentRepository enrollmentRepository) {
        this.classroomService = classroomService;
        this.courseService = courseService;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.examRepository = examRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long profId = SecurityUtil.userId(auth);
        model.addAttribute("pageTitle", "Professeur — Tableau de bord");
        model.addAttribute("coursesCount", courseRepository.countByOwnerProfId(profId));
        model.addAttribute("classroomsCount", classroomRepository.countByOwnerProfId(profId));
        model.addAttribute("examsCount", examRepository.countByCreatedBy(profId));
        model.addAttribute("studentsCount", enrollmentRepository.countStudentsByProfId(profId));
        return "prof/dashboard";
    }

    @GetMapping("/classrooms")
    public String classrooms(Authentication auth, Model model) {
        Long profId = SecurityUtil.userId(auth);
        var classes = classroomService.profClasses(profId);
        
        // Ajouter les compteurs pour chaque classe
        model.addAttribute("pageTitle", "Professeur — Mes classes");
        model.addAttribute("classes", classes);
        model.addAttribute("form", new ClassroomCreateForm());
        model.addAttribute("classroomService", classroomService);
        return "prof/classrooms";
    }

    @PostMapping("/classrooms")
    public String create(Authentication auth,
                         @Valid @ModelAttribute("form") ClassroomCreateForm form,
                         BindingResult br,
                         Model model) {
        Long profId = SecurityUtil.userId(auth);
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Professeur — Mes classes");
            model.addAttribute("classes", classroomService.profClasses(profId));
            model.addAttribute("classroomService", classroomService);
            return "prof/classrooms";
        }
        classroomService.createClass(profId, form.getTitle());
        return "redirect:/prof/classrooms";
    }

    // ===== Vue détaillée d'une classe (style Google Classroom) =====

    @GetMapping("/classrooms/{classroomId}")
    public String viewClassroom(Authentication auth,
                                @PathVariable Long classroomId,
                                Model model) {
        Long profId = SecurityUtil.userId(auth);
        Classroom classroom = classroomService.requireOwned(classroomId, profId);
        
        model.addAttribute("pageTitle", "Classe — " + classroom.getTitle());
        model.addAttribute("classroom", classroom);
        model.addAttribute("posts", classroomService.getPosts(classroomId));
        model.addAttribute("students", classroomService.getEnrolledStudents(classroomId));
        model.addAttribute("courses", classroomService.getClassroomCourses(classroomId));
        model.addAttribute("allProfCourses", courseService.profCourses(profId));
        model.addAttribute("studentCount", classroomService.countStudents(classroomId));
        model.addAttribute("courseCount", classroomService.countCourses(classroomId));
        
        return "prof/classroom_view";
    }

    @PostMapping("/classrooms/{classroomId}/posts")
    public String createPost(Authentication auth,
                            @PathVariable Long classroomId,
                            @RequestParam String type,
                            @RequestParam String content,
                            RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            classroomService.requireOwned(classroomId, profId);
            classroomService.createPost(classroomId, profId, type, content);
            ra.addFlashAttribute("flashSuccess", "Publication créée.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/prof/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/posts/{postId}/delete")
    public String deletePost(Authentication auth,
                            @PathVariable Long classroomId,
                            @PathVariable Long postId,
                            RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            classroomService.deletePost(postId, profId);
            ra.addFlashAttribute("flashSuccess", "Publication supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/prof/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/students/{studentId}/remove")
    public String removeStudent(Authentication auth,
                               @PathVariable Long classroomId,
                               @PathVariable Long studentId,
                               RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            classroomService.removeStudent(classroomId, studentId, profId);
            ra.addFlashAttribute("flashSuccess", "Étudiant retiré de la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/prof/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/courses/attach")
    public String attachCourse(Authentication auth,
                              @PathVariable Long classroomId,
                              @RequestParam Long courseId,
                              RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            classroomService.attachCourse(classroomId, courseId, profId);
            ra.addFlashAttribute("flashSuccess", "Cours ajouté à la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/prof/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/courses/{courseId}/detach")
    public String detachCourse(Authentication auth,
                              @PathVariable Long classroomId,
                              @PathVariable Long courseId,
                              RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            classroomService.detachCourse(classroomId, courseId, profId);
            ra.addFlashAttribute("flashSuccess", "Cours retiré de la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/prof/classrooms/" + classroomId;
    }
}

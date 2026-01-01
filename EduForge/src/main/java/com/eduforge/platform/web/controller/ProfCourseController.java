package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.service.classroom.ClassroomService;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.service.rag.RagIndexService;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.CourseCreateForm;
import com.eduforge.platform.web.dto.forms.CourseUpdateForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prof/courses")
public class ProfCourseController {

    private final CourseService courseService;
    private final ClassroomService classroomService;
    private final RagIndexService ragIndexService;

    public ProfCourseController(CourseService courseService,
                                ClassroomService classroomService,
                                RagIndexService ragIndexService) {
        this.courseService = courseService;
        this.classroomService = classroomService;
        this.ragIndexService = ragIndexService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        Long profId = SecurityUtil.userId(auth);
        model.addAttribute("pageTitle", "Prof — Cours");
        model.addAttribute("courses", courseService.profCourses(profId));
        model.addAttribute("form", new CourseCreateForm());
        return "prof/courses";
    }

    @PostMapping
    public String create(Authentication auth,
                         @Valid @ModelAttribute("form") CourseCreateForm form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Prof — Cours");
            model.addAttribute("courses", courseService.profCourses(profId));
            return "prof/courses";
        }
        Course c = courseService.create(profId, form);
        ra.addFlashAttribute("flashSuccess", "Cours créé.");
        return "redirect:/prof/courses/" + c.getId();
    }

    @GetMapping("/{courseId}")
    public String edit(Authentication auth, @PathVariable Long courseId, Model model) {
        Long profId = SecurityUtil.userId(auth);
        Course c = courseService.requireOwned(courseId, profId);

        CourseUpdateForm f = new CourseUpdateForm();
        f.setTitle(c.getTitle());
        f.setDescription(c.getDescription());
        f.setTextContent(c.getTextContent());

        model.addAttribute("pageTitle", "Prof — Éditer cours");
        model.addAttribute("course", c);
        model.addAttribute("form", f);
        model.addAttribute("materials", courseService.listMaterials(courseId));
        model.addAttribute("classrooms", classroomService.profClasses(profId));
        return "prof/course_edit";
    }

    @PostMapping("/{courseId}/update")
    public String update(Authentication auth,
                         @PathVariable Long courseId,
                         @Valid @ModelAttribute("form") CourseUpdateForm form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        Course c = courseService.requireOwned(courseId, profId);

        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Prof — Éditer cours");
            model.addAttribute("course", c);
            model.addAttribute("materials", courseService.listMaterials(courseId));
            model.addAttribute("classrooms", classroomService.profClasses(profId));
            return "prof/course_edit";
        }

        courseService.update(courseId, profId, form);
        ra.addFlashAttribute("flashSuccess", "Cours mis à jour.");
        return "redirect:/prof/courses/" + courseId;
    }

    @PostMapping("/{courseId}/upload")
    public String upload(Authentication auth,
                         @PathVariable Long courseId,
                         @RequestParam("file") MultipartFile file,
                         RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            courseService.uploadMaterial(courseId, profId, file);
            ra.addFlashAttribute("flashSuccess", "Fichier uploadé et texte extrait.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", rootMessage(e));
        }
        return "redirect:/prof/courses/" + courseId;
    }

    @PostMapping("/{courseId}/attach")
    public String attach(Authentication auth,
                         @PathVariable Long courseId,
                         @RequestParam("classroomId") Long classroomId,
                         RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            courseService.attachToClassroom(courseId, profId, classroomId);
            ra.addFlashAttribute("flashSuccess", "Cours lié à la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", rootMessage(e));
        }
        return "redirect:/prof/courses/" + courseId;
    }

    @PostMapping("/{courseId}/publish")
    public String publish(Authentication auth,
                          @PathVariable Long courseId,
                          @RequestParam(value = "publishType", defaultValue = "PUBLIC") String publishType,
                          @RequestParam(value = "targetClassroomId", required = false) Long targetClassroomId,
                          RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            courseService.publishWithType(courseId, profId, publishType, targetClassroomId);
            String successMsg = publishType.equals("PUBLIC") 
                ? "Cours publié publiquement." 
                : "Cours publié pour la classe sélectionnée.";
            ra.addFlashAttribute("flashSuccess", successMsg);
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", rootMessage(e));
        }
        return "redirect:/prof/courses/" + courseId;
    }

    @PostMapping("/{courseId}/unpublish")
    public String unpublish(Authentication auth,
                            @PathVariable Long courseId,
                            RedirectAttributes ra) {
        Long profId = SecurityUtil.userId(auth);
        try {
            courseService.unpublish(courseId, profId);
            ra.addFlashAttribute("flashSuccess", "Cours repassé en brouillon.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", rootMessage(e));
        }
        return "redirect:/prof/courses/" + courseId;
    }

    // Endpoint manuel /reindex supprimé - auto-indexation activée dans CourseService.update() et uploadMaterial()

    private String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? "Erreur" : m;
    }
}

package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.course.CourseMaterial;
import com.eduforge.platform.service.course.CourseService;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class StudentCourseController {

    private final CourseService courseService;

    public StudentCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/student/courses")
    public String studentCourses(Authentication auth, Model model) {
        Long studentId = SecurityUtil.userId(auth);
        model.addAttribute("pageTitle", "Étudiant — Cours");
        model.addAttribute("courses", courseService.studentCourses(studentId));
        return "student/courses";
    }

    @GetMapping("/course/{courseId}")
    public String viewCourse(Authentication auth, @PathVariable Long courseId, Model model) {
        Long userId = SecurityUtil.userId(auth);

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isProfOwner = isProfOwner(auth, courseId);
        Course c = courseService.getById(courseId);
        
        // Vérifier les permissions : Admin OU Propriétaire OU (PUBLIC + authentifié) OU Étudiant avec accès
        boolean isPublicCourse = c.getStatus() == com.eduforge.platform.domain.course.CourseStatus.PUBLIC;
        boolean isAuthenticated = auth != null && auth.isAuthenticated();
        boolean hasClassroomAccess = courseService.studentCanAccessCourse(userId, courseId);
        
        boolean allowed = isAdmin || isProfOwner || (isPublicCourse && isAuthenticated) || hasClassroomAccess;

        if (!allowed) {
            throw new IllegalArgumentException("Accès refusé au cours.");
        }

        model.addAttribute("pageTitle", "Cours — " + c.getTitle());
        model.addAttribute("course", c);
        model.addAttribute("materials", courseService.listMaterials(courseId));
        model.addAttribute("fullText", courseService.fullCourseText(courseId));
        model.addAttribute("backUrl", resolveBackUrl(auth));
        return "course/view";
    }

    @GetMapping("/course/{courseId}/material/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterial(Authentication auth,
                                                     @PathVariable Long courseId,
                                                     @PathVariable Long materialId) {
        Long userId = SecurityUtil.userId(auth);
        
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean allowed = isAdmin
                || courseService.studentCanAccessCourse(userId, courseId)
                || isProfOwner(auth, courseId);

        if (!allowed) {
            return ResponseEntity.status(403).build();
        }

        CourseMaterial material = courseService.getMaterial(materialId);
        if (material == null || !material.getCourseId().equals(courseId)) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(material.getStoredPath());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        MediaType contentType = material.getType().equalsIgnoreCase("PDF") 
                ? MediaType.APPLICATION_PDF 
                : MediaType.TEXT_PLAIN;

        Resource fileResource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + material.getOriginalName() + "\"")
                .contentType(contentType)
                .body(fileResource);
    }

    private String resolveBackUrl(Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return "/admin/courses";
        boolean isProf = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
        if (isProf) return "/prof/courses";
        return "/student/courses";
    }

    private boolean isProfOwner(Authentication auth, Long courseId) {
        boolean isProf = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
        if (!isProf) return false;
        Long profId = SecurityUtil.userId(auth);
        try {
            courseService.requireOwned(courseId, profId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

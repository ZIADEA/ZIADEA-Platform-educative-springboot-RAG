package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.reviewbook.ReviewBook;
import com.eduforge.platform.service.reviewbook.ReviewBookQuizService;
import com.eduforge.platform.service.reviewbook.ReviewBookService;
import com.eduforge.platform.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student/reviewbook")
public class ReviewBookController {

    private final ReviewBookService reviewBookService;
    private final ReviewBookQuizService quizService;

    public ReviewBookController(ReviewBookService reviewBookService, 
                                ReviewBookQuizService quizService) {
        this.reviewBookService = reviewBookService;
        this.quizService = quizService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        Long studentId = SecurityUtil.userId(auth);
        List<ReviewBook> books = reviewBookService.getStudentBooks(studentId);
        
        model.addAttribute("pageTitle", "Mes documents de révision");
        model.addAttribute("books", books);
        return "student/reviewbook/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("pageTitle", "Uploader un document");
        return "student/reviewbook/upload";
    }

    @GetMapping("/generate")
    public String generateForm(Model model) {
        model.addAttribute("pageTitle", "Générer un cours avec IA");
        return "student/reviewbook/generate";
    }

    @PostMapping("/upload")
    public String upload(Authentication auth,
                        @RequestParam String title,
                        @RequestParam MultipartFile file,
                        RedirectAttributes ra) {
        Long studentId = SecurityUtil.userId(auth);
        
        try {
            ReviewBook book = reviewBookService.upload(studentId, title, file);
            ra.addFlashAttribute("flashSuccess", 
                "Document uploadé avec succès ! Le traitement est en cours...");
            return "redirect:/student/reviewbook/" + book.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/student/reviewbook";
        }
    }

    @PostMapping("/generate")
    public String generateFromPrompt(Authentication auth,
                                     @RequestParam String title,
                                     @RequestParam String prompt,
                                     RedirectAttributes ra) {
        Long studentId = SecurityUtil.userId(auth);
        
        try {
            ReviewBook book = reviewBookService.generateCourseFromPrompt(studentId, title, prompt);
            ra.addFlashAttribute("flashSuccess", 
                "Cours généré avec succès par IA !");
            return "redirect:/student/reviewbook/" + book.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", "Erreur génération: " + e.getMessage());
            return "redirect:/student/reviewbook";
        }
    }

    @GetMapping("/{id}")
    public String view(Authentication auth,
                      @PathVariable Long id,
                      Model model) {
        Long studentId = SecurityUtil.userId(auth);
        ReviewBook book = reviewBookService.requireOwned(id, studentId);
        
        model.addAttribute("pageTitle", book.getTitle());
        model.addAttribute("book", book);
        return "student/reviewbook/view";
    }

    @GetMapping("/{id}/quiz")
    public String startQuiz(Authentication auth,
                           @PathVariable Long id,
                           @RequestParam(defaultValue = "5") int count,
                           @RequestParam(required = false) String focus,
                           Model model) {
        Long studentId = SecurityUtil.userId(auth);
        ReviewBook book = reviewBookService.requireOwned(id, studentId);
        
        if (!book.isReady()) {
            model.addAttribute("pageTitle", book.getTitle());
            model.addAttribute("book", book);
            model.addAttribute("error", "Le document n'est pas encore prêt pour le quiz.");
            return "student/reviewbook/view";
        }

        try {
            var questions = quizService.generateQuiz(book, Math.min(count, 20), focus);
            
            model.addAttribute("pageTitle", "Quiz: " + book.getTitle());
            model.addAttribute("book", book);
            model.addAttribute("questions", questions);
            return "student/reviewbook/quiz";
        } catch (Exception e) {
            model.addAttribute("pageTitle", book.getTitle());
            model.addAttribute("book", book);
            model.addAttribute("error", "Erreur lors de la génération du quiz: " + e.getMessage());
            return "student/reviewbook/view";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(Authentication auth,
                        @PathVariable Long id,
                        RedirectAttributes ra) {
        Long studentId = SecurityUtil.userId(auth);
        
        try {
            reviewBookService.delete(id, studentId);
            ra.addFlashAttribute("flashSuccess", "Document supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        
        return "redirect:/student/reviewbook";
    }
}

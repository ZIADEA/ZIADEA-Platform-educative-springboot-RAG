package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.library.LibraryResource;
import com.eduforge.platform.service.institution.InstitutionService;
import com.eduforge.platform.service.library.LibraryService;
import com.eduforge.platform.util.SecurityUtil;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/institution/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final InstitutionService institutionService;

    public LibraryController(LibraryService libraryService,
                             InstitutionService institutionService) {
        this.libraryService = libraryService;
        this.institutionService = institutionService;
    }

    @GetMapping
    public String list(Authentication auth,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String search,
                       Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        List<LibraryResource> resources;
        if (search != null && !search.isBlank()) {
            resources = libraryService.search(inst.getId(), search);
        } else if (category != null && !category.isBlank()) {
            resources = libraryService.listByCategory(inst.getId(), category);
        } else {
            resources = libraryService.listByInstitution(inst.getId());
        }

        model.addAttribute("pageTitle", "Bibliothèque institutionnelle");
        model.addAttribute("inst", inst);
        model.addAttribute("resources", resources);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", search);
        return "institution/library";
    }

    @GetMapping("/upload")
    public String uploadForm(Authentication auth, Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        model.addAttribute("pageTitle", "Ajouter une ressource");
        model.addAttribute("inst", inst);
        return "institution/library_upload";
    }

    @PostMapping("/upload")
    public String upload(Authentication auth,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam("title") @NotBlank String title,
                         @RequestParam(value = "description", required = false) String description,
                         @RequestParam(value = "category", required = false) String category,
                         @RequestParam(value = "author", required = false) String author,
                         @RequestParam(value = "isbn", required = false) String isbn,
                         @RequestParam(value = "publishedYear", required = false) Integer publishedYear,
                         Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        try {
            libraryService.upload(inst.getId(), ownerId, file, title, description, category, author, isbn, publishedYear);
            return "redirect:/institution/library?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("pageTitle", "Ajouter une ressource");
            model.addAttribute("inst", inst);
            model.addAttribute("error", e.getMessage());
            return "institution/library_upload";
        } catch (Exception e) {
            model.addAttribute("pageTitle", "Ajouter une ressource");
            model.addAttribute("inst", inst);
            model.addAttribute("error", "Erreur lors de l'upload: " + e.getMessage());
            return "institution/library_upload";
        }
    }

    @GetMapping("/{resourceId}")
    public String view(Authentication auth,
                       @PathVariable Long resourceId,
                       Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        LibraryResource resource = libraryService.getById(resourceId)
                .filter(r -> r.getInstitutionId().equals(inst.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Ressource introuvable."));

        model.addAttribute("pageTitle", resource.getTitle());
        model.addAttribute("inst", inst);
        model.addAttribute("resource", resource);
        return "institution/library_view";
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Resource> download(Authentication auth,
                                             @PathVariable Long resourceId) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        LibraryResource lr = libraryService.getById(resourceId)
                .filter(r -> r.getInstitutionId().equals(inst.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Ressource introuvable."));

        Path filePath = libraryService.getFilePath(resourceId);
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        libraryService.incrementDownload(resourceId);

        Resource fileResource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + lr.getOriginalName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(fileResource);
    }

    @PostMapping("/{resourceId}/delete")
    public String delete(Authentication auth,
                         @PathVariable Long resourceId) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        LibraryResource lr = libraryService.getById(resourceId)
                .filter(r -> r.getInstitutionId().equals(inst.getId()))
                .orElse(null);

        if (lr != null) {
            libraryService.delete(resourceId);
        }

        return "redirect:/institution/library";
    }
}

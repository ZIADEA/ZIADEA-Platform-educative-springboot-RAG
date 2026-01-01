package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.Role;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.domain.library.LibraryResource;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.repository.InstitutionRepository;
import com.eduforge.platform.repository.LibraryResourceRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.service.library.LibraryService;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/my-institutions")
public class MyInstitutionsController {

    private final InstitutionRepository institutionRepository;
    private final InstitutionMembershipRepository membershipRepository;
    private final LibraryResourceRepository libraryResourceRepository;
    private final UserRepository userRepository;
    private final LibraryService libraryService;

    public MyInstitutionsController(InstitutionRepository institutionRepository,
                                   InstitutionMembershipRepository membershipRepository,
                                   LibraryResourceRepository libraryResourceRepository,
                                   UserRepository userRepository,
                                   LibraryService libraryService) {
        this.institutionRepository = institutionRepository;
        this.membershipRepository = membershipRepository;
        this.libraryResourceRepository = libraryResourceRepository;
        this.userRepository = userRepository;
        this.libraryService = libraryService;
    }

    @GetMapping
    public String listInstitutions(Authentication auth, Model model) {
        Long userId = SecurityUtil.userId(auth);
        User user = userRepository.findById(userId).orElseThrow();
        
        List<Institution> institutions = new ArrayList<>();
        
        if (user.getRole() == Role.ETUDIANT || user.getRole() == Role.PROF) {
            // Récupérer toutes les institutions où l'utilisateur est approuvé
            var memberships = membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.APPROVED);
            List<Long> institutionIds = memberships.stream()
                    .map(m -> m.getInstitutionId())
                    .collect(Collectors.toList());
            
            if (!institutionIds.isEmpty()) {
                institutions = institutionRepository.findAllById(institutionIds);
            }
        }
        
        model.addAttribute("pageTitle", "Mes Institutions");
        model.addAttribute("institutions", institutions);
        model.addAttribute("userRole", user.getRole().name());
        return "my-institutions/list";
    }

    @GetMapping("/{institutionId}/library")
    public String viewLibrary(Authentication auth,
                             @PathVariable Long institutionId,
                             Model model) {
        Long userId = SecurityUtil.userId(auth);
        
        // Vérifier que l'utilisateur est bien affilié à cette institution
        boolean isAffiliated = membershipRepository.existsByInstitutionIdAndUserIdAndStatus(
                institutionId, userId, MembershipStatus.APPROVED);
        
        if (!isAffiliated) {
            throw new IllegalArgumentException("Vous n'êtes pas affilié à cette institution.");
        }
        
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution introuvable."));
        
        List<LibraryResource> resources = libraryResourceRepository
                .findByInstitutionIdOrderByCreatedAtDesc(institutionId);
        
        model.addAttribute("pageTitle", "Bibliothèque - " + institution.getName());
        model.addAttribute("institution", institution);
        model.addAttribute("resources", resources);
        return "my-institutions/library";
    }

    @GetMapping("/{institutionId}/library/download/{resourceId}")
    public ResponseEntity<Resource> downloadResource(Authentication auth,
                                                      @PathVariable Long institutionId,
                                                      @PathVariable Long resourceId) {
        Long userId = SecurityUtil.userId(auth);
        
        // Vérifier que l'utilisateur est bien affilié à cette institution
        boolean isAffiliated = membershipRepository.existsByInstitutionIdAndUserIdAndStatus(
                institutionId, userId, MembershipStatus.APPROVED);
        
        if (!isAffiliated) {
            return ResponseEntity.status(403).build();
        }
        
        // Vérifier que la ressource appartient à cette institution
        LibraryResource lr = libraryResourceRepository.findById(resourceId)
                .filter(r -> r.getInstitutionId().equals(institutionId))
                .orElse(null);
        
        if (lr == null) {
            return ResponseEntity.notFound().build();
        }
        
        Path filePath = libraryService.getFilePath(resourceId);
        if (filePath == null || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        
        libraryService.incrementDownload(resourceId);
        
        Resource fileResource = new FileSystemResource(filePath);
        
        // Déterminer le type MIME
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (lr.getFileType() != null) {
            switch (lr.getFileType().toUpperCase()) {
                case "PDF" -> mediaType = MediaType.APPLICATION_PDF;
                case "TXT" -> mediaType = MediaType.TEXT_PLAIN;
            }
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + lr.getOriginalName() + "\"")
                .contentType(mediaType)
                .body(fileResource);
    }
}

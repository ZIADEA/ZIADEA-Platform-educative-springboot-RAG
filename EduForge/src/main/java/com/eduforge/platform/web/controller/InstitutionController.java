package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.AffiliationStatus;
import com.eduforge.platform.domain.auth.Role;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.classroom.Classroom;
import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.repository.ClassroomRepository;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.repository.LibraryResourceRepository;
import com.eduforge.platform.repository.ProgramRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.service.catalog.CatalogService;
import com.eduforge.platform.service.classroom.ClassroomService;
import com.eduforge.platform.service.institution.ApprovalService;
import com.eduforge.platform.service.institution.InstitutionService;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.forms.*;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/institution")
public class InstitutionController {

    private final InstitutionService institutionService;
    private final ApprovalService approvalService;
    private final CatalogService catalogService;
    private final ClassroomService classroomService;
    private final ProgramRepository programRepository;
    private final InstitutionMembershipRepository membershipRepository;
    private final LibraryResourceRepository libraryResourceRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public InstitutionController(InstitutionService institutionService,
                                 ApprovalService approvalService,
                                 CatalogService catalogService,
                                 ClassroomService classroomService,
                                 ProgramRepository programRepository,
                                 InstitutionMembershipRepository membershipRepository,
                                 LibraryResourceRepository libraryResourceRepository,
                                 ClassroomRepository classroomRepository,
                                 UserRepository userRepository) {
        this.institutionService = institutionService;
        this.approvalService = approvalService;
        this.catalogService = catalogService;
        this.classroomService = classroomService;
        this.programRepository = programRepository;
        this.membershipRepository = membershipRepository;
        this.libraryResourceRepository = libraryResourceRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        // Compter les membres affiliés directement
        long affiliatedProfsCount = userRepository.countByInstitutionIdAndAffiliationStatusAndRole(
                inst.getId(), AffiliationStatus.AFFILIATED, Role.PROF);
        long affiliatedStudentsCount = userRepository.countByInstitutionIdAndAffiliationStatusAndRole(
                inst.getId(), AffiliationStatus.AFFILIATED, Role.ETUDIANT);
        
        model.addAttribute("pageTitle", "Institution — Tableau de bord");
        model.addAttribute("programsCount", programRepository.countByInstitutionId(inst.getId()));
        model.addAttribute("profsCount", affiliatedProfsCount);
        model.addAttribute("studentsCount", affiliatedStudentsCount);
        model.addAttribute("pendingCount", membershipRepository.countByInstitutionIdAndStatus(inst.getId(), MembershipStatus.PENDING));
        model.addAttribute("libraryCount", libraryResourceRepository.countByInstitutionId(inst.getId()));
        return "institution/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        InstitutionProfileForm form = new InstitutionProfileForm();
        form.setName(inst.getName());
        form.setType(inst.getType().name());
        form.setCountry(inst.getCountry());
        form.setCity(inst.getCity());
        form.setAddress(inst.getAddress());

        model.addAttribute("pageTitle", "Institution — Profil");
        model.addAttribute("inst", inst);
        model.addAttribute("form", form);
        return "institution/profile";
    }

    @PostMapping("/profile")
    public String saveProfile(Authentication auth,
                              @Valid @ModelAttribute("form") InstitutionProfileForm form,
                              BindingResult br,
                              Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Institution — Profil");
            model.addAttribute("inst", inst);
            return "institution/profile";
        }

        Institution updated = institutionService.updateProfile(ownerId, form);
        model.addAttribute("pageTitle", "Institution — Profil");
        model.addAttribute("inst", updated);
        model.addAttribute("form", form);
        model.addAttribute("flashSuccess", "Profil institution mis à jour.");
        return "institution/profile";
    }

    @GetMapping("/approvals")
    public String approvals(Authentication auth, Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        model.addAttribute("pageTitle", "Institution — Approbations");
        model.addAttribute("inst", inst);
        model.addAttribute("pending", approvalService.pendingRequestsEnriched(inst.getId()));
        return "institution/approvals";
    }

    @PostMapping("/approvals/{membershipId}/approve")
    public String approve(Authentication auth, @PathVariable Long membershipId) {
        Long ownerId = SecurityUtil.userId(auth);
        institutionService.getOrCreateByOwner(ownerId); // check existence
        approvalService.approve(membershipId);
        return "redirect:/institution/approvals";
    }

    @PostMapping("/approvals/{membershipId}/reject")
    public String reject(Authentication auth, @PathVariable Long membershipId) {
        Long ownerId = SecurityUtil.userId(auth);
        institutionService.getOrCreateByOwner(ownerId);
        approvalService.reject(membershipId);
        return "redirect:/institution/approvals";
    }

    @GetMapping("/structure")
    public String structure(Authentication auth,
                            @RequestParam(value = "programId", required = false) Long programId,
                            @RequestParam(value = "levelId", required = false) Long levelId,
                            Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);

        model.addAttribute("pageTitle", "Institution — Structure");
        model.addAttribute("inst", inst);
        model.addAttribute("programs", catalogService.programs(inst.getId()));
        model.addAttribute("programForm", new ProgramForm());
        model.addAttribute("levelForm", new LevelForm());
        model.addAttribute("subjectForm", new SubjectForm());

        if (programId != null) {
            model.addAttribute("selectedProgramId", programId);
            model.addAttribute("levels", catalogService.levels(programId));
        }
        if (levelId != null) {
            model.addAttribute("selectedLevelId", levelId);
            model.addAttribute("subjects", catalogService.subjects(levelId));
        }
        return "institution/structure";
    }

    @PostMapping("/structure/program")
    public String addProgram(Authentication auth,
                             @Valid @ModelAttribute("programForm") ProgramForm form,
                             BindingResult br) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        if (!br.hasErrors()) {
            catalogService.addProgram(inst.getId(), form.getName());
        }
        return "redirect:/institution/structure";
    }

    @PostMapping("/structure/level")
    public String addLevel(@RequestParam Long programId,
                           @Valid @ModelAttribute("levelForm") LevelForm form,
                           BindingResult br) {
        if (!br.hasErrors()) {
            catalogService.addLevel(programId, form.getLabel());
        }
        return "redirect:/institution/structure?programId=" + programId;
    }

    @PostMapping("/structure/subject")
    public String addSubject(@RequestParam Long programId,
                             @RequestParam Long levelId,
                             @Valid @ModelAttribute("subjectForm") SubjectForm form,
                             BindingResult br) {
        if (!br.hasErrors()) {
            catalogService.addSubject(levelId, form.getName());
        }
        return "redirect:/institution/structure?programId=" + programId + "&levelId=" + levelId;
    }

    // ===== GESTION DES CLASSES =====

    @GetMapping("/classrooms/create")
    public String createClassroomForm(Authentication auth, Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        var approvedMembers = membershipRepository.findByInstitutionIdAndStatus(inst.getId(), MembershipStatus.APPROVED);
        List<Long> memberIds = approvedMembers.stream().map(m -> m.getUserId()).collect(Collectors.toList());
        List<User> approvedProfs = memberIds.isEmpty() ? List.of() : 
            userRepository.findAllById(memberIds).stream()
                .filter(u -> u.getRole() == Role.PROF)
                .collect(Collectors.toList());
        
        model.addAttribute("pageTitle", "Créer une classe");
        model.addAttribute("inst", inst);
        model.addAttribute("approvedProfs", approvedProfs);
        model.addAttribute("subjects", catalogService.allSubjects(inst.getId()));
        return "institution/classroom_create";
    }

    @GetMapping("/classrooms")
    public String classrooms(Authentication auth, 
                           @RequestParam(required = false) Long subjectId,
                           Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        List<Classroom> classes = classroomRepository.findByInstitutionIdOrderByCreatedAtDesc(inst.getId());
        
        // Filtrer par matière si spécifié
        if (subjectId != null) {
            classes = classes.stream()
                    .filter(c -> subjectId.equals(c.getSubjectId()))
                    .collect(Collectors.toList());
            
            // Ajouter le nom de la matière pour affichage
            catalogService.getSubjectById(subjectId).ifPresent(subject -> {
                model.addAttribute("filterSubjectName", subject.getName());
                model.addAttribute("filterSubjectId", subjectId);
            });
        }
        
        // Récupérer les profs approuvés pour l'affectation
        var approvedMembers = membershipRepository.findByInstitutionIdAndStatus(inst.getId(), MembershipStatus.APPROVED);
        List<Long> memberIds = approvedMembers.stream().map(m -> m.getUserId()).collect(Collectors.toList());
        List<User> approvedProfs = memberIds.isEmpty() ? List.of() : 
            userRepository.findAllById(memberIds).stream()
                .filter(u -> u.getRole() == Role.PROF)
                .collect(Collectors.toList());
        List<User> approvedStudents = memberIds.isEmpty() ? List.of() :
            userRepository.findAllById(memberIds).stream()
                .filter(u -> u.getRole() == Role.ETUDIANT)
                .collect(Collectors.toList());

        model.addAttribute("pageTitle", "Institution — Classes");
        model.addAttribute("inst", inst);
        model.addAttribute("classes", classes);
        model.addAttribute("approvedProfs", approvedProfs);
        model.addAttribute("approvedStudents", approvedStudents);
        model.addAttribute("subjects", catalogService.allSubjects(inst.getId()));
        return "institution/classrooms";
    }

    @PostMapping("/classrooms/create")
    public String createClassroom(Authentication auth,
                                  @RequestParam String title,
                                  @RequestParam(required = false) Long subjectId,
                                  @RequestParam Long profId,
                                  RedirectAttributes ra) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        try {
            // Vérifier que le prof est bien affilié
            if (!membershipRepository.existsByInstitutionIdAndUserIdAndStatus(inst.getId(), profId, MembershipStatus.APPROVED)) {
                throw new IllegalArgumentException("Ce professeur n'est pas affilié à votre institution.");
            }
            
            // Créer la classe avec le prof comme owner
            Classroom classroom = classroomService.createClass(profId, title);
            classroom.setInstitutionId(inst.getId());
            
            // Affecter la matière si fournie
            if (subjectId != null) {
                classroom.setSubjectId(subjectId);
            }
            
            classroomRepository.save(classroom);
            
            ra.addFlashAttribute("flashSuccess", "Classe créée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/institution/classrooms";
    }

    @GetMapping("/classrooms/{classroomId}")
    public String viewClassroom(Authentication auth,
                                @PathVariable Long classroomId,
                                Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .filter(c -> inst.getId().equals(c.getInstitutionId()))
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
        
        var approvedMembers = membershipRepository.findByInstitutionIdAndStatus(inst.getId(), MembershipStatus.APPROVED);
        List<Long> memberIds = approvedMembers.stream().map(m -> m.getUserId()).collect(Collectors.toList());
        
        List<User> allMembers = memberIds.isEmpty() ? List.of() : userRepository.findAllById(memberIds);
        
        List<User> approvedStudents = allMembers.stream()
                .filter(u -> u.getRole() == Role.ETUDIANT)
                .collect(Collectors.toList());
        
        // Profs affiliés
        List<User> approvedProfs = allMembers.stream()
                .filter(u -> u.getRole() == Role.PROF)
                .collect(Collectors.toList());
        
        // Étudiants déjà inscrits
        List<User> enrolledStudents = classroomService.getEnrolledStudents(classroomId);
        List<Long> enrolledIds = enrolledStudents.stream().map(User::getId).collect(Collectors.toList());
        
        // Étudiants disponibles (pas encore inscrits)
        List<User> availableStudents = approvedStudents.stream()
                .filter(s -> !enrolledIds.contains(s.getId()))
                .collect(Collectors.toList());
        
        // Prof responsable actuel
        User currentProf = userRepository.findById(classroom.getOwnerProfId()).orElse(null);
        
        // Charger la matière si assignée
        String subjectName = null;
        if (classroom.getSubjectId() != null) {
            subjectName = catalogService.getSubjectById(classroom.getSubjectId())
                    .map(s -> s.getName())
                    .orElse(null);
        }

        model.addAttribute("pageTitle", "Classe — " + classroom.getTitle());
        model.addAttribute("inst", inst);
        model.addAttribute("classroom", classroom);
        model.addAttribute("subjectName", subjectName);
        model.addAttribute("enrolledStudents", enrolledStudents);
        model.addAttribute("availableStudents", availableStudents);
        model.addAttribute("approvedProfs", approvedProfs);
        model.addAttribute("currentProf", currentProf);
        model.addAttribute("studentCount", classroomService.countStudents(classroomId));
        return "institution/classroom_view";
    }

    @PostMapping("/classrooms/{classroomId}/enroll")
    public String enrollStudent(Authentication auth,
                                @PathVariable Long classroomId,
                                @RequestParam Long studentId,
                                RedirectAttributes ra) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        try {
            Classroom classroom = classroomRepository.findById(classroomId)
                    .filter(c -> inst.getId().equals(c.getInstitutionId()))
                    .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
            
            // Vérifier que l'étudiant est affilié
            if (!membershipRepository.existsByInstitutionIdAndUserIdAndStatus(inst.getId(), studentId, MembershipStatus.APPROVED)) {
                throw new IllegalArgumentException("Cet étudiant n'est pas affilié à votre institution.");
            }
            
            classroomService.joinByCode(studentId, classroom.getCode());
            ra.addFlashAttribute("flashSuccess", "Étudiant inscrit à la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/institution/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/unenroll/{studentId}")
    public String unenrollStudent(Authentication auth,
                                  @PathVariable Long classroomId,
                                  @PathVariable Long studentId,
                                  RedirectAttributes ra) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        try {
            Classroom classroom = classroomRepository.findById(classroomId)
                    .filter(c -> inst.getId().equals(c.getInstitutionId()))
                    .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
            
            classroomService.removeStudent(classroomId, studentId, classroom.getOwnerProfId());
            ra.addFlashAttribute("flashSuccess", "Étudiant retiré de la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/institution/classrooms/" + classroomId;
    }

    @PostMapping("/classrooms/{classroomId}/assign-prof")
    public String assignProfToClassroom(Authentication auth,
                                        @PathVariable Long classroomId,
                                        @RequestParam Long profId,
                                        RedirectAttributes ra) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        try {
            Classroom classroom = classroomRepository.findById(classroomId)
                    .filter(c -> inst.getId().equals(c.getInstitutionId()))
                    .orElseThrow(() -> new IllegalArgumentException("Classe introuvable."));
            
            // Vérifier que le prof est bien affilié
            if (!membershipRepository.existsByInstitutionIdAndUserIdAndStatus(inst.getId(), profId, MembershipStatus.APPROVED)) {
                throw new IllegalArgumentException("Ce professeur n'est pas affilié à votre institution.");
            }
            
            // Vérifier que c'est bien un prof
            User prof = userRepository.findById(profId)
                    .filter(u -> u.getRole() == Role.PROF)
                    .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé ou n'est pas un professeur."));
            
            classroom.setOwnerProfId(profId);
            classroomRepository.save(classroom);
            
            ra.addFlashAttribute("flashSuccess", "Professeur " + prof.getFullName() + " assigné comme responsable de la classe.");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/institution/classrooms/" + classroomId;
    }

    // ========== Liste des membres affiliés ==========
    
    @GetMapping("/members")
    public String members(Authentication auth,
                          @RequestParam(value = "tab", defaultValue = "profs") String tab,
                          Model model) {
        Long ownerId = SecurityUtil.userId(auth);
        Institution inst = institutionService.getOrCreateByOwner(ownerId);
        
        // Professeurs affiliés
        List<User> affiliatedProfs = userRepository.findByInstitutionIdAndAffiliationStatusAndRole(
                inst.getId(), AffiliationStatus.AFFILIATED, Role.PROF);
        
        // Étudiants affiliés
        List<User> affiliatedStudents = userRepository.findByInstitutionIdAndAffiliationStatusAndRole(
                inst.getId(), AffiliationStatus.AFFILIATED, Role.ETUDIANT);
        
        model.addAttribute("pageTitle", "Institution — Membres affiliés");
        model.addAttribute("inst", inst);
        model.addAttribute("affiliatedProfs", affiliatedProfs);
        model.addAttribute("affiliatedStudents", affiliatedStudents);
        model.addAttribute("profsCount", affiliatedProfs.size());
        model.addAttribute("studentsCount", affiliatedStudents.size());
        model.addAttribute("activeTab", tab);
        
        return "institution/members";
    }
}

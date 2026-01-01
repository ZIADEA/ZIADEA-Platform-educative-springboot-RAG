package com.eduforge.platform.service.auth;

import com.eduforge.platform.domain.auth.AccountStatus;
import com.eduforge.platform.domain.auth.AffiliationStatus;
import com.eduforge.platform.domain.auth.Role;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.InstitutionMembership;
import com.eduforge.platform.domain.institution.InstitutionType;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.repository.InstitutionRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.web.dto.forms.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final InstitutionRepository institutions;
    private final InstitutionMembershipRepository memberships;
    private final PasswordEncoder encoder;

    public RegistrationService(UserRepository users,
                              InstitutionRepository institutions,
                              InstitutionMembershipRepository memberships,
                              PasswordEncoder encoder) {
        this.users = users;
        this.institutions = institutions;
        this.memberships = memberships;
        this.encoder = encoder;
    }

    @Transactional
    public User register(RegisterForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà.");
        }

        Role role = switch (form.getAccountType()) {
            case "ETUDIANT" -> Role.ETUDIANT;
            case "PROF" -> Role.PROF;
            case "INSTITUTION" -> Role.INSTITUTION_MANAGER;
            default -> throw new IllegalArgumentException("Type de compte invalide.");
        };

        // Déterminer le statut initial
        AccountStatus accountStatus;
        AffiliationStatus affiliationStatus;
        Long institutionId = null;

        if (role == Role.INSTITUTION_MANAGER) {
            // Création d'une institution : compte actif immédiatement
            accountStatus = AccountStatus.ACTIVE;
            affiliationStatus = AffiliationStatus.INDEPENDENT;
        } else if (form.wantsInstitutionAffiliation()) {
            // Prof ou étudiant qui veut s'affilier à une institution
            Institution inst = institutions.findById(form.getInstitutionId())
                    .orElseThrow(() -> new IllegalArgumentException("Institution introuvable."));
            
            if (role == Role.PROF) {
                // Le prof doit être approuvé par l'institution
                accountStatus = AccountStatus.PENDING;
                affiliationStatus = AffiliationStatus.PENDING;
                institutionId = inst.getId();
            } else {
                // L'étudiant peut rejoindre directement une institution
                accountStatus = AccountStatus.ACTIVE;
                affiliationStatus = AffiliationStatus.AFFILIATED;
                institutionId = inst.getId();
            }
        } else {
            // Utilisateur indépendant (prof libre ou étudiant libre)
            accountStatus = AccountStatus.ACTIVE;
            affiliationStatus = AffiliationStatus.INDEPENDENT;
        }

        // Créer l'utilisateur
        User u = new User(
                form.getFullName().trim(),
                email,
                encoder.encode(form.getPassword()),
                role,
                accountStatus,
                institutionId,
                affiliationStatus
        );
        
        if (form.getPhone() != null && !form.getPhone().isBlank()) {
            u.setPhone(form.getPhone().trim());
        }
        
        User savedUser = users.save(u);

        // Si c'est un gestionnaire d'institution, créer l'institution
        if (role == Role.INSTITUTION_MANAGER) {
            createInstitutionForManager(savedUser, form);
        }

        // Si c'est un prof qui demande une affiliation, créer la demande
        if (role == Role.PROF && form.wantsInstitutionAffiliation()) {
            createAffiliationRequest(savedUser, form.getInstitutionId());
        }

        return savedUser;
    }

    private void createInstitutionForManager(User manager, RegisterForm form) {
        InstitutionType type = InstitutionType.UNIVERSITE;
        if (form.getInstitutionType() != null && !form.getInstitutionType().isBlank()) {
            try {
                type = InstitutionType.valueOf(form.getInstitutionType());
            } catch (IllegalArgumentException ignored) {}
        }

        String name = form.getInstitutionName();
        if (name == null || name.isBlank()) {
            name = "Institution de " + manager.getFullName();
        }

        Institution inst = new Institution(
                manager.getId(),
                name.trim(),
                type,
                form.getCountry() != null ? form.getCountry().trim() : "France",
                form.getCity() != null ? form.getCity().trim() : "Paris",
                form.getAddress() != null ? form.getAddress().trim() : null
        );
        
        institutions.save(inst);
    }

    private void createAffiliationRequest(User prof, Long institutionId) {
        InstitutionMembership membership = new InstitutionMembership(
                institutionId,
                prof.getId(),
                MembershipStatus.PENDING
        );
        memberships.save(membership);
    }

    /**
     * Permet à un utilisateur existant de demander une affiliation à une institution
     */
    @Transactional
    public void requestAffiliation(Long userId, Long institutionId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        
        Institution inst = institutions.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution introuvable."));

        // Vérifier qu'il n'y a pas déjà une demande en cours
        if (memberships.existsByInstitutionIdAndUserIdAndStatus(institutionId, userId, MembershipStatus.PENDING)) {
            throw new IllegalArgumentException("Une demande d'affiliation est déjà en cours.");
        }

        // Créer la demande
        InstitutionMembership membership = new InstitutionMembership(
                institutionId,
                userId,
                MembershipStatus.PENDING
        );
        memberships.save(membership);

        // Mettre à jour le statut de l'utilisateur
        user.setInstitutionId(institutionId);
        user.setAffiliationStatus(AffiliationStatus.PENDING);
        users.save(user);
    }

    /**
     * Permet à un utilisateur de quitter une institution
     */
    @Transactional
    public void leaveInstitution(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
        
        if (user.getInstitutionId() == null) {
            throw new IllegalArgumentException("Vous n'êtes affilié à aucune institution.");
        }

        // Supprimer l'adhésion
        memberships.deleteByUserIdAndInstitutionId(userId, user.getInstitutionId());

        // Mettre à jour l'utilisateur
        user.setInstitutionId(null);
        user.setProgramId(null);
        user.setLevelId(null);
        user.setAffiliationStatus(AffiliationStatus.INDEPENDENT);
        users.save(user);
    }
}

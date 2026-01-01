package com.eduforge.platform.service.institution;

import com.eduforge.platform.domain.auth.AffiliationStatus;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.institution.InstitutionMembership;
import com.eduforge.platform.domain.institution.MembershipStatus;
import com.eduforge.platform.repository.InstitutionMembershipRepository;
import com.eduforge.platform.repository.UserRepository;
import com.eduforge.platform.web.dto.MembershipRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalService {

    private final InstitutionMembershipRepository memberships;
    private final UserRepository userRepository;

    public ApprovalService(InstitutionMembershipRepository memberships, UserRepository userRepository) {
        this.memberships = memberships;
        this.userRepository = userRepository;
    }

    public List<InstitutionMembership> pendingForInstitution(Long institutionId) {
        return memberships.findByInstitutionIdAndStatus(institutionId, MembershipStatus.PENDING);
    }

    public List<MembershipRequestDTO> pendingRequestsEnriched(Long institutionId) {
        List<InstitutionMembership> pending = memberships.findByInstitutionIdAndStatus(institutionId, MembershipStatus.PENDING);
        return pending.stream().map(m -> {
            User user = userRepository.findById(m.getUserId()).orElse(null);
            if (user != null) {
                return new MembershipRequestDTO(m, user.getFullName(), user.getEmail(), user.getRole().name());
            } else {
                return new MembershipRequestDTO(m, "Utilisateur inconnu", "", "");
            }
        }).collect(Collectors.toList());
    }

    @Transactional
    public void approve(Long membershipId) {
        InstitutionMembership m = memberships.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        m.setStatus(MembershipStatus.APPROVED);
        
        // Mettre à jour le statut de l'utilisateur
        User user = userRepository.findById(m.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setAffiliationStatus(AffiliationStatus.AFFILIATED);
        user.setInstitutionId(m.getInstitutionId());
        userRepository.save(user);
    }

    @Transactional
    public void reject(Long membershipId) {
        InstitutionMembership m = memberships.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        m.setStatus(MembershipStatus.REJECTED);
        
        // Réinitialiser le statut de l'utilisateur
        User user = userRepository.findById(m.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
        user.setAffiliationStatus(AffiliationStatus.REJECTED);
        userRepository.save(user);
    }
}

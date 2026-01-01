package com.eduforge.platform.repository;

import com.eduforge.platform.domain.institution.InstitutionMembership;
import com.eduforge.platform.domain.institution.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionMembershipRepository extends JpaRepository<InstitutionMembership, Long> {
    
    List<InstitutionMembership> findByInstitutionIdAndStatus(Long institutionId, MembershipStatus status);
    
    List<InstitutionMembership> findByUserIdAndStatus(Long userId, MembershipStatus status);
    
    Optional<InstitutionMembership> findByInstitutionIdAndUserId(Long institutionId, Long userId);
    
    List<InstitutionMembership> findByUserId(Long userId);
    
    List<InstitutionMembership> findByInstitutionId(Long institutionId);
    
    boolean existsByInstitutionIdAndUserIdAndStatus(Long institutionId, Long userId, MembershipStatus status);
    
    void deleteByUserIdAndInstitutionId(Long userId, Long institutionId);
    
    long countByInstitutionIdAndStatus(Long institutionId, MembershipStatus status);
}

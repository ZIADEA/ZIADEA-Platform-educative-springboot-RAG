package com.eduforge.platform.repository;

import com.eduforge.platform.domain.auth.AffiliationStatus;
import com.eduforge.platform.domain.auth.Role;
import com.eduforge.platform.domain.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRole(Role role);
    List<User> findByRoleIn(List<Role> roles);
    
    // Membres affiliés à une institution
    List<User> findByInstitutionIdAndAffiliationStatusAndRole(Long institutionId, AffiliationStatus status, Role role);
    List<User> findByInstitutionIdAndAffiliationStatus(Long institutionId, AffiliationStatus status);
    long countByInstitutionIdAndAffiliationStatusAndRole(Long institutionId, AffiliationStatus status, Role role);
}

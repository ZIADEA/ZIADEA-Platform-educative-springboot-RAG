package com.eduforge.platform.repository;

import com.eduforge.platform.domain.institution.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByOwnerUserId(Long ownerUserId);
}

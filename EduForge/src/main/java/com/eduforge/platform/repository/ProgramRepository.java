package com.eduforge.platform.repository;

import com.eduforge.platform.domain.catalog.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByInstitutionIdOrderByNameAsc(Long institutionId);
    long countByInstitutionId(Long institutionId);
}

package com.eduforge.platform.repository;

import com.eduforge.platform.domain.catalog.AcademicLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, Long> {
    List<AcademicLevel> findByProgramIdOrderByLabelAsc(Long programId);
}

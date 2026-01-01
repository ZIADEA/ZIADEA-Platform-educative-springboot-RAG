package com.eduforge.platform.repository;

import com.eduforge.platform.domain.catalog.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByLevelIdOrderByNameAsc(Long levelId);
}

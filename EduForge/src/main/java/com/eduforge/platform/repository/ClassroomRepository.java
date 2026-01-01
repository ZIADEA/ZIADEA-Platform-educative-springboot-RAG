package com.eduforge.platform.repository;

import com.eduforge.platform.domain.classroom.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByOwnerProfIdOrderByCreatedAtDesc(Long ownerProfId);
    List<Classroom> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
    Optional<Classroom> findByCodeIgnoreCase(String code);
    long countByOwnerProfId(Long ownerProfId);
    long countByInstitutionId(Long institutionId);
}

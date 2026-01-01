package com.eduforge.platform.repository;

import com.eduforge.platform.domain.library.LibraryResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryResourceRepository extends JpaRepository<LibraryResource, Long> {
    
    List<LibraryResource> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
    
    List<LibraryResource> findByInstitutionIdAndCategoryOrderByCreatedAtDesc(Long institutionId, String category);
    
    List<LibraryResource> findByInstitutionIdAndIsPublicTrueOrderByCreatedAtDesc(Long institutionId);
    
    List<LibraryResource> findByInstitutionIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            Long institutionId, String title);
    
    long countByInstitutionId(Long institutionId);
}

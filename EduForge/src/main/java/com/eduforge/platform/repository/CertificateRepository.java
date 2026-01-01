package com.eduforge.platform.repository;

import com.eduforge.platform.domain.certificate.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByStudentIdOrderByIssuedAtDesc(Long studentId);
    
    Optional<Certificate> findByCertificateCode(String certificateCode);
    
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
    
    boolean existsByStudentIdAndClassroomId(Long studentId, Long classroomId);
}

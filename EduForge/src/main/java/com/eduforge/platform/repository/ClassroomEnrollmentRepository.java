package com.eduforge.platform.repository;

import com.eduforge.platform.domain.classroom.ClassroomEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassroomEnrollmentRepository extends JpaRepository<ClassroomEnrollment, Long> {
    Optional<ClassroomEnrollment> findByClassroomIdAndStudentId(Long classroomId, Long studentId);
    List<ClassroomEnrollment> findByStudentId(Long studentId);
    List<ClassroomEnrollment> findByClassroomId(Long classroomId);
    long countByStudentId(Long studentId);
    long countByClassroomId(Long classroomId);
    
    @Query("SELECT COUNT(DISTINCT e.studentId) FROM ClassroomEnrollment e WHERE e.classroomId IN " +
           "(SELECT c.id FROM Classroom c WHERE c.ownerProfId = :profId)")
    long countStudentsByProfId(Long profId);
}

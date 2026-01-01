package com.eduforge.platform.repository;

import com.eduforge.platform.domain.classroom.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {
    
    Optional<StudentProgress> findByClassroomIdAndStudentId(Long classroomId, Long studentId);
    
    List<StudentProgress> findByStudentIdOrderByUpdatedAtDesc(Long studentId);
    
    List<StudentProgress> findByClassroomIdOrderByXpPointsDesc(Long classroomId);
    
    List<StudentProgress> findByClassroomIdOrderByAverageScoreDesc(Long classroomId);
    
    boolean existsByClassroomIdAndStudentId(Long classroomId, Long studentId);
    
    @Query("SELECT AVG(p.averageScore) FROM StudentProgress p WHERE p.classroomId = :classroomId")
    Double averageScoreByClassroomId(Long classroomId);
    
    long countByClassroomId(Long classroomId);
    
    @Query("SELECT p FROM StudentProgress p WHERE p.classroomId = :classroomId ORDER BY p.xpPoints DESC LIMIT :limit")
    List<StudentProgress> findTopByClassroom(Long classroomId, int limit);
}

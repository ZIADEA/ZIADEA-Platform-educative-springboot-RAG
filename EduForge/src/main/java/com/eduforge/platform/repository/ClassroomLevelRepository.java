package com.eduforge.platform.repository;

import com.eduforge.platform.domain.classroom.ClassroomLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomLevelRepository extends JpaRepository<ClassroomLevel, Long> {
    
    List<ClassroomLevel> findByClassroomIdOrderByLevelNumberAsc(Long classroomId);
    
    Optional<ClassroomLevel> findByClassroomIdAndLevelNumber(Long classroomId, Integer levelNumber);
    
    long countByClassroomId(Long classroomId);
    
    void deleteByClassroomId(Long classroomId);
}

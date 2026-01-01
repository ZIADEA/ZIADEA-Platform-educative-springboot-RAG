package com.eduforge.platform.repository;

import com.eduforge.platform.domain.course.ClassroomCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomCourseRepository extends JpaRepository<ClassroomCourse, Long> {
    List<ClassroomCourse> findByClassroomId(Long classroomId);
    List<ClassroomCourse> findByCourseId(Long courseId);
    boolean existsByClassroomIdAndCourseId(Long classroomId, Long courseId);
    Optional<ClassroomCourse> findByClassroomIdAndCourseId(Long classroomId, Long courseId);
    long countByClassroomId(Long classroomId);
}

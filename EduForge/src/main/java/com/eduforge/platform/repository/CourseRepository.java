package com.eduforge.platform.repository;

import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.course.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByOwnerProfIdOrderByCreatedAtDesc(Long ownerProfId);
    Optional<Course> findByIdAndOwnerProfId(Long id, Long ownerProfId);

    // Catalogue public
    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);
    List<Course> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(CourseStatus status, String title);
    long countByStatus(CourseStatus status);
    long countByOwnerProfId(Long ownerProfId);
}

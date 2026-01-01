package com.eduforge.platform.repository;

import com.eduforge.platform.domain.classroom.ClassroomPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassroomPostRepository extends JpaRepository<ClassroomPost, Long> {
    List<ClassroomPost> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
}

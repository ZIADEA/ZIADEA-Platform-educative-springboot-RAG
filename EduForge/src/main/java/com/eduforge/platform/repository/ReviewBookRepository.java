package com.eduforge.platform.repository;

import com.eduforge.platform.domain.reviewbook.ReviewBook;
import com.eduforge.platform.domain.reviewbook.ReviewBookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewBookRepository extends JpaRepository<ReviewBook, Long> {
    
    List<ReviewBook> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    List<ReviewBook> findByStudentIdAndStatusOrderByCreatedAtDesc(Long studentId, ReviewBookStatus status);
    
    List<ReviewBook> findByStatus(ReviewBookStatus status);
    
    long countByStudentId(Long studentId);
}

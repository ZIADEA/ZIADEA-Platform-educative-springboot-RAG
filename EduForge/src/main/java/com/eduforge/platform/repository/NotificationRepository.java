package com.eduforge.platform.repository;

import com.eduforge.platform.domain.messaging.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    void deleteByUserId(Long userId);
}

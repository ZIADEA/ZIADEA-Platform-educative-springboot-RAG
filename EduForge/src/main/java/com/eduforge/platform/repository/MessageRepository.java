package com.eduforge.platform.repository;

import com.eduforge.platform.domain.messaging.Message;
import com.eduforge.platform.domain.messaging.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Messages reçus par un utilisateur
    List<Message> findByRecipientIdAndIsArchivedFalseOrderByCreatedAtDesc(Long recipientId);
    
    // Messages envoyés par un utilisateur  
    List<Message> findBySenderIdAndMessageTypeOrderByCreatedAtDesc(Long senderId, MessageType type);
    
    // Messages d'une classe
    List<Message> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
    
    // Messages non lus
    List<Message> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
    
    // Nombre de messages non lus
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    
    // Conversation entre 2 utilisateurs
    @Query("SELECT m FROM Message m WHERE m.messageType = 'DIRECT' " +
           "AND ((m.senderId = :user1 AND m.recipientId = :user2) " +
           "OR (m.senderId = :user2 AND m.recipientId = :user1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(Long user1, Long user2);
}

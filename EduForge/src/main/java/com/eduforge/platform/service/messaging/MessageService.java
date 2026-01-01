package com.eduforge.platform.service.messaging;

import com.eduforge.platform.domain.messaging.*;
import com.eduforge.platform.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messages;

    public MessageService(MessageRepository messages) {
        this.messages = messages;
    }

    public List<Message> getInbox(Long userId) {
        return messages.findByRecipientIdAndIsArchivedFalseOrderByCreatedAtDesc(userId);
    }

    public List<Message> getSent(Long userId) {
        try {
            return messages.findBySenderIdAndMessageTypeOrderByCreatedAtDesc(userId, MessageType.DIRECT);
        } catch (Exception e) {
            // En cas d'erreur, retourner une liste vide plutôt qu'une exception
            return List.of();
        }
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        return messages.findConversation(userId1, userId2);
    }

    public List<Message> getClassroomMessages(Long classroomId) {
        return messages.findByClassroomIdOrderByCreatedAtDesc(classroomId);
    }

    public long getUnreadCount(Long userId) {
        return messages.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public Message sendDirect(Long senderId, Long recipientId, String subject, String content) {
        Message msg = new Message(senderId, recipientId, subject, content, MessageType.DIRECT);
        return messages.save(msg);
    }

    @Transactional
    public Message sendToClassroom(Long senderId, Long classroomId, String subject, String content) {
        Message msg = new Message(classroomId, senderId, content, MessageType.CLASSROOM);
        msg.setSubject(subject);
        return messages.save(msg);
    }

    @Transactional
    public Message sendAnnouncement(Long senderId, String subject, String content) {
        Message msg = new Message(senderId, null, subject, content, MessageType.ANNOUNCEMENT);
        return messages.save(msg);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        messages.findById(messageId)
                .filter(m -> m.getRecipientId() != null && m.getRecipientId().equals(userId))
                .ifPresent(m -> {
                    m.setIsRead(true);
                    messages.save(m);
                });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Message> unread = messages.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Message m : unread) {
            m.setIsRead(true);
        }
        messages.saveAll(unread);
    }

    @Transactional
    public void delete(Long messageId, Long userId) {
        messages.findById(messageId)
                .filter(m -> m.getSenderId().equals(userId) || 
                        (m.getRecipientId() != null && m.getRecipientId().equals(userId)))
                .ifPresent(messages::delete);
    }

    public List<Message> searchMessages(Long userId, String keyword) {
        // Simple search by returning inbox messages and filtering
        List<Message> inbox = messages.findByRecipientIdAndIsArchivedFalseOrderByCreatedAtDesc(userId);
        String lowerKeyword = keyword.toLowerCase();
        return inbox.stream()
                .filter(m -> (m.getSubject() != null && m.getSubject().toLowerCase().contains(lowerKeyword))
                        || (m.getContent() != null && m.getContent().toLowerCase().contains(lowerKeyword)))
                .toList();
    }
}

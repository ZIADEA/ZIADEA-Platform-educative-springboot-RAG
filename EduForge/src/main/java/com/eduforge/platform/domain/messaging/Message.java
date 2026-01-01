package com.eduforge.platform.domain.messaging;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Message entre utilisateurs ou dans une classe
 */
@Entity
@Table(name = "message", indexes = {
    @Index(name = "idx_msg_class", columnList = "classroom_id"),
    @Index(name = "idx_msg_sender", columnList = "sender_id"),
    @Index(name = "idx_msg_recipient", columnList = "recipient_id")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "recipient_id")
    private Long recipientId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 30)
    private MessageType messageType = MessageType.DIRECT;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "is_archived")
    private Boolean isArchived = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "read_at")
    private Instant readAt;

    public Message() {}

    public Message(Long senderId, Long recipientId, String subject, String content, MessageType messageType) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.subject = subject;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = Instant.now();
    }

    public Message(Long classroomId, Long senderId, String content, MessageType messageType) {
        this.classroomId = classroomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = Instant.now();
    }
}

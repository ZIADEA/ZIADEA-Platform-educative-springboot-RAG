package com.eduforge.platform.domain.messaging;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Notification utilisateur
 */
@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_notif_user", columnList = "user_id")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 500)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", length = 30)
    private NotificationType notificationType = NotificationType.INFO;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "read_at")
    private Instant readAt;

    public Notification() {}

    public Notification(Long userId, String title, String content, NotificationType type) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.notificationType = type;
        this.createdAt = Instant.now();
    }

    public Notification(Long userId, String title, String content, String link, NotificationType type) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.link = link;
        this.notificationType = type;
        this.createdAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = Instant.now();
    }
}

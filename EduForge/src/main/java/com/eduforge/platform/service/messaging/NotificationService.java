package com.eduforge.platform.service.messaging;

import com.eduforge.platform.domain.messaging.*;
import com.eduforge.platform.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public List<Notification> getAll(Long userId) {
        return notifications.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnread(Long userId) {
        return notifications.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notifications.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification notify(Long userId, NotificationType type, String title, String message) {
        Notification n = new Notification(userId, title, message, type);
        return notifications.save(n);
    }

    @Transactional
    public Notification notify(Long userId, NotificationType type, String title, String message, String link) {
        Notification n = new Notification(userId, title, message, type);
        n.setLink(link);
        return notifications.save(n);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notifications.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .ifPresent(n -> {
                    n.setIsRead(true);
                    notifications.save(n);
                });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notifications.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notifications.saveAll(unread);
    }

    @Transactional
    public void delete(Long notificationId, Long userId) {
        notifications.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .ifPresent(notifications::delete);
    }

    @Transactional
    public void deleteAll(Long userId) {
        notifications.deleteByUserId(userId);
    }

    // --- Notifications prédéfinies ---

    public void notifyNewExam(Long studentId, String examTitle, Long examId) {
        notify(studentId, NotificationType.EXAM,
                "Nouvel examen disponible",
                "Un nouvel examen '" + examTitle + "' est disponible.",
                "/student/exam/" + examId);
    }

    public void notifyExamGraded(Long studentId, String examTitle, int score, Long attemptId) {
        notify(studentId, NotificationType.GRADE,
                "Résultat d'examen",
                "Votre examen '" + examTitle + "' a été noté: " + score + "%",
                "/student/exam/result/" + attemptId);
    }

    public void notifyNewAssignment(Long studentId, String assignmentTitle, Long assignmentId) {
        notify(studentId, NotificationType.INFO,
                "Nouveau devoir",
                "Un nouveau devoir '" + assignmentTitle + "' a été assigné.",
                "/student/assignment/" + assignmentId);
    }

    public void notifyAffiliationApproved(Long userId, String institutionName) {
        notify(userId, NotificationType.SUCCESS,
                "Affiliation approuvée",
                "Votre demande d'affiliation à '" + institutionName + "' a été approuvée.",
                "/prof/institution");
    }

    public void notifyAffiliationRejected(Long userId, String institutionName) {
        notify(userId, NotificationType.WARNING,
                "Affiliation refusée",
                "Votre demande d'affiliation à '" + institutionName + "' a été refusée.",
                "/prof/institution");
    }

    public void notifyNewMessage(Long userId, String senderName) {
        notify(userId, NotificationType.MESSAGE,
                "Nouveau message",
                "Vous avez reçu un message de " + senderName + ".",
                "/messages/inbox");
    }

    public void notifyClassroomJoined(Long profId, String studentName, String classroomName) {
        notify(profId, NotificationType.INFO,
                "Nouvel étudiant",
                studentName + " a rejoint votre classe '" + classroomName + "'.",
                null);
    }

    public void notifyCourseEnrollment(Long studentId, String courseTitle, Long courseId) {
        notify(studentId, NotificationType.ENROLLMENT,
                "Inscription confirmée",
                "Vous êtes inscrit au cours '" + courseTitle + "'.",
                "/student/course/" + courseId);
    }

    public void notifyCertificateEarned(Long studentId, String courseTitle, Long certificateId) {
        notify(studentId, NotificationType.SUCCESS,
                "Certificat obtenu !",
                "Félicitations ! Vous avez obtenu le certificat pour '" + courseTitle + "'.",
                "/student/certificates/" + certificateId);
    }
}

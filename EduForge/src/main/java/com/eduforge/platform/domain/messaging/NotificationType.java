package com.eduforge.platform.domain.messaging;

/**
 * Type de notification
 */
public enum NotificationType {
    INFO,           // Information générale
    SUCCESS,        // Succès (examen réussi, etc.)
    WARNING,        // Avertissement
    ERROR,          // Erreur
    EXAM,           // Notification d'examen
    GRADE,          // Notification de note
    MESSAGE,        // Nouveau message
    ENROLLMENT,     // Inscription
    SYSTEM          // Notification système
}

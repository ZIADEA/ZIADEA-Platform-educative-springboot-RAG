package com.eduforge.platform.domain.messaging;

/**
 * Type de message
 */
public enum MessageType {
    DIRECT,         // Message direct entre 2 utilisateurs
    CLASSROOM,      // Message à toute la classe
    ANNOUNCEMENT,   // Annonce officielle
    SYSTEM          // Message système
}

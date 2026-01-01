package com.eduforge.platform.domain.exam;

/**
 * Statut d'une tentative d'examen
 */
public enum AttemptStatus {
    IN_PROGRESS,    // En cours
    SUBMITTED,      // Soumis
    GRADED,         // Noté
    TIMED_OUT,      // Temps écoulé
    ABANDONED       // Abandonné
}

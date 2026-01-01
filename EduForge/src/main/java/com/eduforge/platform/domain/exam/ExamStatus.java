package com.eduforge.platform.domain.exam;

/**
 * Statut d'un examen
 */
public enum ExamStatus {
    DRAFT,          // En cours de création
    PUBLISHED,      // Publié, visible aux étudiants
    SCHEDULED,      // Programmé avec date/heure
    IN_PROGRESS,    // En cours de passation
    CLOSED,         // Fermé, plus de soumissions
    ARCHIVED        // Archivé
}

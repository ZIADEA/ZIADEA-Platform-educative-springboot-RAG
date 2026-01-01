package com.eduforge.platform.domain.course;

/**
 * Statut de publication d'un cours
 * - DRAFT: Brouillon, visible uniquement par le prof
 * - PUBLIC: Publié publiquement, visible par tous
 * - CLASSE: Publié pour une classe spécifique uniquement
 */
public enum CourseStatus {
    DRAFT,
    PUBLIC,
    CLASSE,
    PUBLISHED  // Deprecated: use PUBLIC or CLASSE instead
}

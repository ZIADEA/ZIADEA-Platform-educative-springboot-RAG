package com.eduforge.platform.domain.auth;

/**
 * Statut d'affiliation d'un utilisateur à une institution
 */
public enum AffiliationStatus {
    INDEPENDENT,     // Utilisateur libre, sans institution
    PENDING,         // Demande d'affiliation en attente
    AFFILIATED,      // Affilié et approuvé
    REJECTED,        // Demande rejetée
    SUSPENDED        // Affiliation suspendue
}

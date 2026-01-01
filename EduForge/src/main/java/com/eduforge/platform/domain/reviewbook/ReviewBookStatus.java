package com.eduforge.platform.domain.reviewbook;

public enum ReviewBookStatus {
    PENDING,    // En attente de traitement OCR
    PROCESSING, // OCR en cours
    READY,      // Texte extrait, prêt pour quiz
    FAILED      // Erreur lors de l'extraction
}

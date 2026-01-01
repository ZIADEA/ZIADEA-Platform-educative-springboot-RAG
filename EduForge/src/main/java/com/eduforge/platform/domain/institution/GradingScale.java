package com.eduforge.platform.domain.institution;

/**
 * Échelle de notation utilisée par une institution ou classe
 */
public enum GradingScale {
    SCALE_20,      // Note sur 20 (système français)
    SCALE_100,     // Note sur 100 (pourcentage)
    SCALE_10,      // Note sur 10
    SCALE_5,       // Note sur 5 (GPA style)
    LETTER_GRADE   // A, B, C, D, F
}

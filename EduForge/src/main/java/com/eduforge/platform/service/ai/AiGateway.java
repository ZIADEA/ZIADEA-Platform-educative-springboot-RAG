package com.eduforge.platform.service.ai;

public interface AiGateway {
    /**
     * Retourne STRICTEMENT un JSON (String) respectant le schéma demandé par l’agent.
     */
    String generateJson(String systemInstruction, String userPrompt);    
    /**
     * Génère du texte avec une image (pour OCR, analyse d'image, etc.)
     * @param systemInstruction Instructions système
     * @param userPrompt Prompt utilisateur
     * @param base64Image Image encodée en base64
     * @param mimeType Type MIME de l'image (image/jpeg, image/png, image/webp)
     * @return Texte généré
     */
    default String generateWithImage(String systemInstruction, String userPrompt, 
                                     String base64Image, String mimeType) {
        throw new UnsupportedOperationException("Vision non supportée par ce provider.");
    }
        String name();
}

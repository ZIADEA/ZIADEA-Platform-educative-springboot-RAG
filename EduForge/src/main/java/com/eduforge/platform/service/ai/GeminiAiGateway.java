package com.eduforge.platform.service.ai;

import com.eduforge.platform.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary; // Ajouté pour le conflit de beans
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory; // Ajouté pour corriger l'erreur de compilation
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
@Primary // Ajouté : Dit à Spring de choisir Gemini plutôt que le Mock par défaut
public class GeminiAiGateway implements AiGateway {

    private final AiProperties props;
    private final RestClient rest;
    private final ObjectMapper om = new ObjectMapper();

    public GeminiAiGateway(AiProperties props) {
        this.props = props;
        
        // --- CORRECTION DE L'ERREUR REQUESTFACTORY ---
        // On crée la factory d'abord pour configurer les timeouts
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(props.getGemini().getTimeoutSeconds()));
        // ----------------------------------------------

        this.rest = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .requestFactory(factory) // On passe l'objet factory ici
                .defaultHeader("x-goog-api-key", props.getGemini().getApiKey() == null ? "" : props.getGemini().getApiKey())
                .build();
    }

    @Override
    public String generateJson(String systemInstruction, String userPrompt) {
        if (props.getGemini().getApiKey() == null || props.getGemini().getApiKey().isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY manquante.");
        }

        String model = props.getGemini().getModel();
        String path = "/models/" + model + ":generateContent";

        String body = """
        {
          "systemInstruction": { "parts": [ { "text": %s } ] },
          "contents": [
            { "role": "user", "parts": [ { "text": %s } ] }
          ],
          "generationConfig": {
            "temperature": 0.2,
            "responseMimeType": "application/json"
          }
        }
        """.formatted(quote(systemInstruction), quote(userPrompt));

        String raw = rest.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = om.readTree(raw);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode()) throw new IllegalArgumentException("Réponse Gemini inattendue.");
            return textNode.asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur parsing Gemini.", e);
        }
    }

    @Override
    public String generateWithImage(String systemInstruction, String userPrompt, 
                                    String base64Image, String mimeType) {
        if (props.getGemini().getApiKey() == null || props.getGemini().getApiKey().isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY manquante.");
        }

        // Utiliser le modèle Vision (gemini-1.5-flash supporte les images)
        String model = props.getGemini().getModel();
        String path = "/models/" + model + ":generateContent";

        // Construction du body avec image inline
        String body = """
        {
          "systemInstruction": { "parts": [ { "text": %s } ] },
          "contents": [
            {
              "role": "user",
              "parts": [
                { "text": %s },
                {
                  "inlineData": {
                    "mimeType": "%s",
                    "data": "%s"
                  }
                }
              ]
            }
          ],
          "generationConfig": {
            "temperature": 0.1
          }
        }
        """.formatted(quote(systemInstruction), quote(userPrompt), mimeType, base64Image);

        String raw = rest.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = om.readTree(raw);
            JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
            if (textNode.isMissingNode()) {
                throw new IllegalArgumentException("Réponse Gemini Vision inattendue.");
            }
            return textNode.asText();
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur parsing Gemini Vision: " + e.getMessage(), e);
        }
    }

    @Override
    public String name() { return "GEMINI"; }

    private String quote(String s) {
        try { return om.writeValueAsString(s); }
        catch (Exception e) { return "\"\""; }
    }
}
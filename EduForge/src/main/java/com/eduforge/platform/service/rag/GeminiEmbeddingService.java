package com.eduforge.platform.service.rag;

import com.eduforge.platform.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Service pour générer des embeddings via l'API Gemini.
 * Utilise le modèle text-embedding-004 qui produit des vecteurs de 768 dimensions.
 */
@Service
public class GeminiEmbeddingService {

    private static final String EMBEDDING_MODEL = "text-embedding-004";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:embedContent?key=%s";
    private static final int EMBEDDING_DIMENSION = 768;

    private final AppProperties props;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiEmbeddingService(AppProperties props) {
        this.props = props;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Génère un embedding pour un texte donné.
     * @param text Le texte à encoder
     * @return Un tableau de floats représentant l'embedding (768 dimensions)
     * @throws EmbeddingException Si l'API échoue
     */
    public float[] embed(String text) {
        String apiKey = props.getAi().getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new EmbeddingException("Clé API Gemini non configurée");
        }

        try {
            String url = String.format(API_URL, EMBEDDING_MODEL, apiKey);
            
            // Construire le body JSON
            String requestBody = objectMapper.writeValueAsString(new EmbedRequest(text));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new EmbeddingException("Erreur API Gemini: " + response.statusCode() + " - " + response.body());
            }

            // Parser la réponse
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode valuesNode = root.path("embedding").path("values");

            if (valuesNode.isMissingNode() || !valuesNode.isArray()) {
                throw new EmbeddingException("Réponse Gemini invalide: pas de valeurs d'embedding");
            }

            float[] embedding = new float[EMBEDDING_DIMENSION];
            int i = 0;
            for (JsonNode val : valuesNode) {
                if (i >= EMBEDDING_DIMENSION) break;
                embedding[i++] = val.floatValue();
            }

            if (i != EMBEDDING_DIMENSION) {
                throw new EmbeddingException("Embedding incomplet: " + i + " valeurs au lieu de " + EMBEDDING_DIMENSION);
            }

            return embedding;

        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Erreur lors de la génération d'embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Génère des embeddings en batch (plus efficace pour plusieurs textes).
     * Gemini ne supporte pas nativement le batch, donc on fait des appels séquentiels.
     */
    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream()
                .map(this::embed)
                .toList();
    }

    /**
     * Vérifie si le service est configuré et opérationnel.
     */
    public boolean isAvailable() {
        String apiKey = props.getAi().getGeminiApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Retourne la dimension des embeddings.
     */
    public int getDimension() {
        return EMBEDDING_DIMENSION;
    }

    // Classes internes pour la sérialisation JSON
    private record EmbedRequest(Content content) {
        EmbedRequest(String text) {
            this(new Content(List.of(new Part(text))));
        }
    }

    private record Content(List<Part> parts) {}
    private record Part(String text) {}

    public static class EmbeddingException extends RuntimeException {
        public EmbeddingException(String message) {
            super(message);
        }
        public EmbeddingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

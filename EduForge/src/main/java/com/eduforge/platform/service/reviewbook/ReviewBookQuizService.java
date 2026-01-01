package com.eduforge.platform.service.reviewbook;

import com.eduforge.platform.config.AiProperties;
import com.eduforge.platform.domain.reviewbook.ReviewBook;
import com.eduforge.platform.domain.reviewbook.ReviewBookStatus;
import com.eduforge.platform.service.ai.AiGateway;
import com.eduforge.platform.service.ai.AiProvider;
import com.eduforge.platform.service.ai.MockAiGateway;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service pour générer des quiz personnalisés à partir du contenu extrait d'un ReviewBook
 */
@Service
public class ReviewBookQuizService {

    private static final Logger log = LoggerFactory.getLogger(ReviewBookQuizService.class);

    public record ReviewQuizQuestion(
        String question,
        Map<String, String> choices,
        String correct,
        String explanation
    ) {}

    private final AiGateway gemini;
    private final MockAiGateway mock;
    private final AiProperties aiProps;
    private final ObjectMapper om = new ObjectMapper();

    public ReviewBookQuizService(AiGateway gemini, MockAiGateway mock, AiProperties aiProps) {
        this.gemini = gemini;
        this.mock = mock;
        this.aiProps = aiProps;
    }

    /**
     * Génère un quiz à partir du texte extrait d'un ReviewBook
     */
    public List<ReviewQuizQuestion> generateQuiz(ReviewBook book, int questionCount, String focusTopic) {
        if (book.getStatus() != ReviewBookStatus.READY) {
            throw new IllegalArgumentException("Le document n'est pas encore prêt. Statut: " + book.getStatus());
        }

        String extractedText = book.getExtractedText();
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("Aucun contenu extrait du document.");
        }

        // Limiter le contexte à ~8000 caractères pour éviter de dépasser les limites
        String context = extractedText.length() > 8000 
            ? extractedText.substring(0, 8000) + "..." 
            : extractedText;

        String system = """
            Tu es un superviseur pédagogique expert.
            Tu vas créer des questions QCM basées UNIQUEMENT sur le contenu fourni.
            
            Règles:
            - Questions pertinentes et variées
            - 1 seule bonne réponse parmi A/B/C/D
            - Propositions plausibles mais distinctes
            - Explications courtes et claires
            - Sortie en JSON strict
            """;

        String focusHint = (focusTopic != null && !focusTopic.isBlank()) 
            ? "Focus particulier sur: " + focusTopic 
            : "";

        String user = """
            CONTENU DU DOCUMENT:
            %s

            CONSIGNES:
            - Génère exactement %d questions QCM
            %s
            
            Format JSON:
            {
              "questions": [
                {
                  "question": "...",
                  "choices": {"A":"...", "B":"...", "C":"...", "D":"..."},
                  "correct": "A|B|C|D",
                  "explanation": "..."
                }
              ]
            }
            """.formatted(context, questionCount, focusHint);

        AiGateway provider = resolveProvider();
        String json;
        try {
            json = provider.generateJson(system, user);
        } catch (Exception e) {
            log.warn("Erreur provider {}, fallback au mock: {}", provider.name(), e.getMessage());
            json = mock.generateJson(system, user);
        }

        try {
            return parseQuestions(json);
        } catch (Exception e) {
            log.warn("Parsing échoué, génération par défaut: {}", e.getMessage());
            return generateDefaultQuestions(questionCount);
        }
    }

    private AiGateway resolveProvider() {
        try {
            AiProvider p = AiProvider.valueOf(aiProps.getProvider().toUpperCase(Locale.ROOT));
            return (p == AiProvider.GEMINI) ? gemini : mock;
        } catch (Exception e) {
            return mock;
        }
    }

    private List<ReviewQuizQuestion> parseQuestions(String json) {
        String cleaned = cleanJson(json);
        List<ReviewQuizQuestion> result = new ArrayList<>();

        try {
            JsonNode root = om.readTree(cleaned);
            JsonNode qs = root.get("questions");
            if (qs == null || !qs.isArray()) {
                throw new IllegalArgumentException("Format JSON invalide.");
            }

            for (JsonNode qn : qs) {
                try {
                    String question = qn.get("question").asText();
                    JsonNode choices = qn.get("choices");
                    Map<String, String> choicesMap = new LinkedHashMap<>();
                    choicesMap.put("A", choices.get("A").asText());
                    choicesMap.put("B", choices.get("B").asText());
                    choicesMap.put("C", choices.get("C").asText());
                    choicesMap.put("D", choices.get("D").asText());
                    String correct = qn.get("correct").asText().toUpperCase();
                    String explanation = qn.has("explanation") ? qn.get("explanation").asText() : "";
                    
                    if (Set.of("A", "B", "C", "D").contains(correct)) {
                        result.add(new ReviewQuizQuestion(question, choicesMap, correct, explanation));
                    }
                } catch (Exception e) {
                    log.debug("Question ignorée: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Parsing échoué: " + e.getMessage(), e);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Aucune question valide générée.");
        }
        return result;
    }

    private List<ReviewQuizQuestion> generateDefaultQuestions(int count) {
        List<ReviewQuizQuestion> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, String> choices = new LinkedHashMap<>();
            choices.put("A", "Option A");
            choices.put("B", "Option B");
            choices.put("C", "Option C");
            choices.put("D", "Option D");
            result.add(new ReviewQuizQuestion(
                "Question " + (i + 1) + " (générée par défaut)",
                choices, "A", "Explication par défaut."
            ));
        }
        return result;
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        else if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) s = s.substring(start, end + 1);
        return s.trim();
    }
}

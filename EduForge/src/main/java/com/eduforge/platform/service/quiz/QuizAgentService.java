package com.eduforge.platform.service.quiz;

import com.eduforge.platform.config.AiProperties;
import com.eduforge.platform.config.QuizProperties;
import com.eduforge.platform.domain.quiz.Difficulty;
import com.eduforge.platform.repository.QuizAttemptRepository;
import com.eduforge.platform.service.ai.AiGateway;
import com.eduforge.platform.service.ai.AiProvider;
import com.eduforge.platform.service.ai.MockAiGateway;
import com.eduforge.platform.service.rag.RagIndexService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuizAgentService {

    private static final Logger log = LoggerFactory.getLogger(QuizAgentService.class);

    // Record pour QCM
    public record GeneratedQuestion(String question, Map<String,String> choices, String correct, String explanation) {}
    
    // Record pour question ouverte
    public record GeneratedOpenQuestion(String question, String expectedAnswer, String gradingCriteria, int maxPoints, String explanation) {}
    
    // Record unifié pour tout type de question
    public record UnifiedQuestion(
        String type, // "MCQ" ou "OPEN_ENDED"
        String question,
        Map<String,String> choices, // null si OPEN_ENDED
        String correct, // null si OPEN_ENDED
        String expectedAnswer, // null si MCQ
        String gradingCriteria, // null si MCQ
        Integer maxPoints, // null si MCQ
        String explanation
    ) {}

    private final AiProperties aiProps;
    private final QuizProperties quizProps;
    private final RagIndexService rag;
    private final QuizAttemptRepository attempts;
    private final AiGateway gemini;
    private final MockAiGateway mock;
    private final ObjectMapper om = new ObjectMapper();

    public QuizAgentService(AiProperties aiProps,
                            QuizProperties quizProps,
                            RagIndexService rag,
                            QuizAttemptRepository attempts,
                            AiGateway gemini,  // GeminiAiGateway @Service
                            MockAiGateway mock) {
        this.aiProps = aiProps;
        this.quizProps = quizProps;
        this.rag = rag;
        this.attempts = attempts;
        this.gemini = gemini;
        this.mock = mock;
    }

    public Difficulty decideDifficulty(Long studentId, Long courseId) {
        var last = attempts.findTop20ByStudentIdAndCourseIdOrderByCreatedAtDesc(studentId, courseId);
        if (last.isEmpty()) return Difficulty.EASY;

        int avg = (int)Math.round(last.stream().mapToInt(a -> a.getScorePercent()).average().orElse(0));
        if (avg >= 85) return Difficulty.HARD;
        if (avg >= 60) return Difficulty.MEDIUM;
        return Difficulty.EASY;
    }

    public int decideQuestionCount(Difficulty diff) {
        int min = quizProps.getMinQuestions();
        int max = quizProps.getMaxQuestions();
        return switch (diff) {
            case EASY -> min;
            case MEDIUM -> Math.min(max, Math.max(min, 8));
            case HARD -> max;
        };
    }

    /**
     * Génère un quiz mixte avec QCM et questions ouvertes
     * @param mcqCount nombre de QCM
     * @param openCount nombre de questions ouvertes
     */
    public List<UnifiedQuestion> generateMixedQuiz(Long courseId, Long studentId, String studentQuery, int mcqCount, int openCount) {
        String q = (studentQuery == null || studentQuery.isBlank())
                ? "points importants du cours"
                : studentQuery.trim();

        var hits = rag.searchTopK(courseId, q);
        if (hits.isEmpty()) {
            throw new IllegalArgumentException("RAG: aucun passage pertinent. Clique 'Indexer (RAG)' côté professeur.");
        }

        StringBuilder context = new StringBuilder();
        context.append("CONTEXTE (extraits du cours). Utilise UNIQUEMENT ce contenu.\n\n");
        for (var h : hits) {
            context.append("- Chunk ").append(h.chunkIndex()).append(" (score ").append(h.score()).append("):\n");
            context.append(h.excerpt()).append("\n\n");
        }

        Difficulty diff = decideDifficulty(studentId, courseId);

        String system = """
            Tu es un superviseur pédagogique strict.
            Règles absolues:
            - Génère des questions uniquement à partir du CONTEXTE fourni.
            - Interdiction d'utiliser des connaissances externes.
            - Sortie STRICTEMENT en JSON valide.
            - Pour les QCM: 1 seule réponse correcte parmi A/B/C/D, propositions plausibles.
            - Pour les questions ouvertes: critères de notation clairs.
            - Ajoute une explication courte pour chaque question.
            """;

        String user = """
            %s

            OBJECTIF:
            - Générer %d questions QCM et %d questions ouvertes.
            - Difficulté: %s
            - Thème souhaité: "%s"
            
            Format JSON EXACT:
            {
              "questions": [
                {
                  "type": "MCQ",
                  "question": "...",
                  "choices": {"A":"...", "B":"...", "C":"...", "D":"..."},
                  "correct": "A|B|C|D",
                  "explanation": "..."
                },
                {
                  "type": "OPEN_ENDED",
                  "question": "...",
                  "expectedAnswer": "...",
                  "gradingCriteria": "...",
                  "maxPoints": 10,
                  "explanation": "..."
                }
              ]
            }
            """.formatted(context, mcqCount, openCount, diff.name(), q);

        AiGateway provider = resolveProvider();
        String json;
        try {
            json = provider.generateJson(system, user);
        } catch (Exception e) {
            log.warn("Erreur provider {}, fallback au mock: {}", provider.name(), e.getMessage());
            json = mock.generateJson(system, user);
        }

        try {
            return parseMixedQuestions(json);
        } catch (Exception e) {
            log.warn("Parsing JSON échoué ({}), génération de questions par défaut", e.getMessage());
            return generateDefaultMixedQuestions(mcqCount, openCount);
        }
    }

    private List<UnifiedQuestion> parseMixedQuestions(String json) {
        String cleaned = cleanJson(json);
        List<UnifiedQuestion> result = new ArrayList<>();
        
        try {
            JsonNode root = om.readTree(cleaned);
            JsonNode qs = root.get("questions");
            if (qs == null || !qs.isArray()) {
                throw new IllegalArgumentException("JSON invalide: questions manquant.");
            }

            for (JsonNode qn : qs) {
                try {
                    String type = qn.has("type") ? qn.get("type").asText().toUpperCase() : "MCQ";
                    String question = mustText(qn, "question");
                    String explanation = qn.has("explanation") ? qn.get("explanation").asText() : "";

                    if ("OPEN_ENDED".equals(type)) {
                        String expected = qn.has("expectedAnswer") ? qn.get("expectedAnswer").asText() : "";
                        String criteria = qn.has("gradingCriteria") ? qn.get("gradingCriteria").asText() : "";
                        int maxPts = qn.has("maxPoints") ? qn.get("maxPoints").asInt() : 10;
                        result.add(new UnifiedQuestion("OPEN_ENDED", question, null, null, expected, criteria, maxPts, explanation));
                    } else {
                        JsonNode choices = qn.get("choices");
                        if (choices == null) continue;
                        Map<String, String> map = new LinkedHashMap<>();
                        map.put("A", mustText(choices, "A"));
                        map.put("B", mustText(choices, "B"));
                        map.put("C", mustText(choices, "C"));
                        map.put("D", mustText(choices, "D"));
                        String correct = mustText(qn, "correct").trim().toUpperCase();
                        if (!Set.of("A", "B", "C", "D").contains(correct)) continue;
                        result.add(new UnifiedQuestion("MCQ", question, map, correct, null, null, null, explanation));
                    }
                } catch (Exception qe) {
                    log.warn("Question ignorée: {}", qe.getMessage());
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

    private List<UnifiedQuestion> generateDefaultMixedQuestions(int mcqCount, int openCount) {
        List<UnifiedQuestion> result = new ArrayList<>();
        for (int i = 0; i < mcqCount; i++) {
            Map<String, String> choices = new LinkedHashMap<>();
            choices.put("A", "Option A");
            choices.put("B", "Option B");
            choices.put("C", "Option C");
            choices.put("D", "Option D");
            result.add(new UnifiedQuestion("MCQ", "Question QCM " + (i + 1) + " (générée par défaut)", 
                choices, "A", null, null, null, "Explication par défaut."));
        }
        for (int i = 0; i < openCount; i++) {
            result.add(new UnifiedQuestion("OPEN_ENDED", "Question ouverte " + (i + 1) + " (générée par défaut)",
                null, null, "Réponse attendue", "Critères de notation", 10, "Explication par défaut."));
        }
        return result;
    }

    public List<GeneratedQuestion> generateQuiz(Long courseId, Long studentId, String studentQuery, int count) {
        // 1) RAG retrieve
        String q = (studentQuery == null || studentQuery.isBlank())
                ? "points importants du cours"
                : studentQuery.trim();

        var hits = rag.searchTopK(courseId, q);
        if (hits.isEmpty()) {
            throw new IllegalArgumentException("RAG: aucun passage pertinent. Clique 'Indexer (RAG)' côté professeur.");
        }

        StringBuilder context = new StringBuilder();
        context.append("CONTEXTE (extraits du cours). Utilise UNIQUEMENT ce contenu.\n\n");
        for (var h : hits) {
            context.append("- Chunk ").append(h.chunkIndex()).append(" (score ").append(h.score()).append("):\n");
            context.append(h.excerpt()).append("\n\n");
        }

        // 2) Agent instruction (contrôle strict)
        Difficulty diff = decideDifficulty(studentId, courseId);

        String system = """
        Tu es un superviseur pédagogique strict.
        Règles absolues:
        - Génère des QCM uniquement à partir du CONTEXTE fourni.
        - Interdiction d'utiliser des connaissances externes.
        - Sortie STRICTEMENT en JSON.
        - 1 seule réponse correcte parmi A/B/C/D.
        - Propositions plausibles, pas triviales.
        - Ajoute une explication courte, basée sur le contexte.
        """;

        String user = """
        %s

        OBJECTIF:
        - Générer %d questions QCM.
        - Difficulté: %s
        - Si la question de l’étudiant est: "%s", adapte les questions à cela.
        - Réponse en JSON au format EXACT:

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

        IMPORTANT:
        - Le champ "questions" doit contenir EXACTEMENT %d éléments.
        """.formatted(context, count, diff.name(), q, count);

        // 3) Provider
        AiGateway provider = resolveProvider();
        String json;
        try {
            log.debug("Génération quiz avec provider: {}", provider.name());
            json = provider.generateJson(system, user);
            log.debug("Réponse JSON brute (200 premiers chars): {}", 
                json != null && json.length() > 200 ? json.substring(0, 200) + "..." : json);
        } catch (Exception e) {
            log.warn("Erreur provider {}, fallback au mock: {}", provider.name(), e.getMessage());
            json = mock.generateJson(system, user);
        }

        // 4) Validate & parse - avec fallback au mock si JSON invalide
        try {
            return parseAndValidate(json, count);
        } catch (Exception e) {
            log.warn("Parsing JSON échoué ({}), utilisation du mock", e.getMessage());
            String mockJson = mock.generateJson(system, user);
            return parseAndValidate(mockJson, count);
        }
    }

    private AiGateway resolveProvider() {
        AiProvider p = AiProvider.valueOf(aiProps.getProvider().toUpperCase(Locale.ROOT));
        return (p == AiProvider.GEMINI) ? gemini : mock;
    }

    private List<GeneratedQuestion> parseAndValidate(String json, int count) {
        log.debug("Parsing JSON quiz...");
        // Nettoyage du JSON (supprimer backticks markdown, espaces, etc.)
        String cleaned = cleanJson(json);
        log.debug("JSON nettoyé: {}", cleaned.length() > 500 ? cleaned.substring(0, 500) + "..." : cleaned);
        
        try {
            JsonNode root = om.readTree(cleaned);
            JsonNode qs = root.get("questions");
            if (qs == null || !qs.isArray()) {
                log.error("JSON invalide - questions manquant. JSON reçu: {}", cleaned);
                throw new IllegalArgumentException("JSON invalide: questions manquant.");
            }
            
            // Validation souple: accepter si au moins 1 question, warning si différent de count
            if (qs.isEmpty()) {
                log.error("JSON invalide - aucune question. JSON reçu: {}", cleaned);
                throw new IllegalArgumentException("JSON invalide: aucune question générée.");
            }

            List<GeneratedQuestion> out = new ArrayList<>();
            for (JsonNode qn : qs) {
                try {
                    String q = mustText(qn, "question");
                    JsonNode choices = qn.get("choices");
                    if (choices == null) throw new IllegalArgumentException("choices manquant.");
                    Map<String,String> map = new LinkedHashMap<>();
                    map.put("A", mustText(choices, "A"));
                    map.put("B", mustText(choices, "B"));
                    map.put("C", mustText(choices, "C"));
                    map.put("D", mustText(choices, "D"));
                    String correct = mustText(qn, "correct").trim().toUpperCase(Locale.ROOT);
                    if (!Set.of("A","B","C","D").contains(correct)) throw new IllegalArgumentException("correct invalide: " + correct);
                    String exp = mustText(qn, "explanation");
                    out.add(new GeneratedQuestion(q, map, correct, exp));
                } catch (Exception qe) {
                    log.warn("Question ignorée (erreur parsing): {}", qe.getMessage());
                    // Continue avec les autres questions
                }
            }
            
            if (out.isEmpty()) {
                log.error("Aucune question valide après parsing. JSON: {}", cleaned);
                throw new IllegalArgumentException("JSON invalide: aucune question valide générée.");
            }
            
            log.debug("Quiz généré avec {} questions", out.size());
            return out;
        } catch (Exception e) {
            log.error("Échec parsing JSON IA: {}", e.getMessage());
            throw new IllegalArgumentException("Échec parsing/validation du JSON IA: " + e.getMessage(), e);
        }
    }

    /**
     * Nettoie le JSON brut retourné par l'IA (supprime markdown, espaces, etc.)
     */
    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        
        // Supprimer les blocs markdown ```json ... ```
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        
        // Trouver le premier { et le dernier }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }
        
        return s.trim();
    }

    private String mustText(JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v == null || v.asText().isBlank()) throw new IllegalArgumentException("Champ manquant: " + key);
        return v.asText().trim();
    }

    /**
     * Génère un quiz avec une difficulté spécifiée (utilisé pour régénération adaptative)
     */
    public List<GeneratedQuestion> generateQuizWithDifficulty(Long courseId, Long studentId, String studentQuery, int count, Difficulty forcedDifficulty) {
        String q = (studentQuery == null || studentQuery.isBlank())
                ? "points importants du cours"
                : studentQuery.trim();

        var hits = rag.searchTopK(courseId, q);
        if (hits.isEmpty()) {
            throw new IllegalArgumentException("RAG: aucun passage pertinent. Clique 'Indexer (RAG)' côté professeur.");
        }

        StringBuilder context = new StringBuilder();
        context.append("CONTEXTE (extraits du cours). Utilise UNIQUEMENT ce contenu.\n\n");
        for (var h : hits) {
            context.append("- Chunk ").append(h.chunkIndex()).append(" (score ").append(h.score()).append("):\n");
            context.append(h.excerpt()).append("\n\n");
        }

        String difficultyInstruction = switch (forcedDifficulty) {
            case EASY -> "Niveau: FACILE - Questions de base, définitions simples, réponses évidentes.";
            case MEDIUM -> "Niveau: MOYEN - Questions de compréhension, application directe.";
            case HARD -> "Niveau: DIFFICILE - Questions d'analyse, cas complexes, nuances fines.";
        };

        String system = """
        Tu es un superviseur pédagogique strict.
        Règles absolues:
        - Génère des QCM uniquement à partir du CONTEXTE fourni.
        - Interdiction d'utiliser des connaissances externes.
        - Sortie STRICTEMENT en JSON.
        - 1 seule réponse correcte parmi A/B/C/D.
        - Propositions plausibles, pas triviales.
        """ + "\n" + difficultyInstruction;

        String userPrompt = context + "\n\nGénère " + count + " questions QCM (JSON).\n" +
                "Format exact:\n" +
                "{\"questions\":[\n" +
                "  {\"q\":\"...\", \"A\":\"...\", \"B\":\"...\", \"C\":\"...\", \"D\":\"...\", \"correct\":\"...\", \"explanation\":\"...\"}\n" +
                "]}\n";

        String raw = resolveProvider().generateJson(system, userPrompt);
        return parseAndValidate(raw, count);
    }

    /**
     * Record pour le résultat du grading d'une réponse ouverte
     */
    public record GradingResult(int score, String feedback) {}

    /**
     * Grade une réponse ouverte avec l'IA
     * @param questionText la question posée
     * @param expectedAnswer la réponse attendue / éléments clés
     * @param gradingRubric grille d'évaluation (peut être null)
     * @param studentAnswer la réponse de l'étudiant
     * @return score (0-100) et feedback détaillé
     */
    public GradingResult gradeOpenEndedAnswer(String questionText, String expectedAnswer, 
                                              String gradingRubric, String studentAnswer) {
        String system = """
        Tu es un correcteur pédagogique expert et bienveillant.
        Ta mission: évaluer une réponse d'étudiant à une question ouverte.
        
        Règles de notation:
        - Compare la réponse de l'étudiant avec la réponse attendue
        - Identifie les éléments clés présents ou absents
        - Sois précis dans ton évaluation (score de 0 à 100)
        - Fournis un feedback constructif et encourageant
        - Si une grille d'évaluation est fournie, respecte-la strictement
        
        Critères généraux:
        - 90-100: Excellente réponse, tous les éléments clés présents, bien formulé
        - 70-89: Bonne réponse, la plupart des éléments présents, quelques lacunes mineures
        - 50-69: Réponse acceptable, éléments principaux présents mais incomplets
        - 30-49: Réponse insuffisante, éléments clés manquants ou incorrects
        - 0-29: Réponse très faible ou hors sujet
        
        Réponds UNIQUEMENT en JSON strict avec cette structure:
        {
          "score": <nombre entre 0 et 100>,
          "feedback": "<explication détaillée et constructive>"
        }
        """;

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("QUESTION:\n").append(questionText).append("\n\n");
        userPrompt.append("RÉPONSE ATTENDUE / ÉLÉMENTS CLÉS:\n").append(expectedAnswer).append("\n\n");
        
        if (gradingRubric != null && !gradingRubric.trim().isEmpty()) {
            userPrompt.append("GRILLE D'ÉVALUATION:\n").append(gradingRubric).append("\n\n");
        }
        
        userPrompt.append("RÉPONSE DE L'ÉTUDIANT:\n").append(studentAnswer).append("\n\n");
        userPrompt.append("Évalue cette réponse et fournis le score et le feedback en JSON.");

        try {
            String raw = resolveProvider().generateJson(system, userPrompt.toString());
            JsonNode root = om.readTree(raw);
            
            int score = root.path("score").asInt(50); // défaut 50 si erreur
            String feedback = root.path("feedback").asText("Réponse évaluée.");
            
            // Validation du score
            if (score < 0) score = 0;
            if (score > 100) score = 100;
            
            return new GradingResult(score, feedback);
        } catch (Exception e) {
            log.error("Erreur lors du grading AI: {}", e.getMessage(), e);
            return new GradingResult(50, 
                "Erreur technique lors de l'évaluation automatique. " +
                "Un correcteur humain vérifiera votre réponse.");
        }
    }
}

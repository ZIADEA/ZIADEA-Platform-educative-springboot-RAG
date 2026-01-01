package com.eduforge.platform.service.rag;

import com.eduforge.platform.config.AppProperties;
import com.eduforge.platform.domain.rag.CourseChunk;
import com.eduforge.platform.repository.CourseChunkRepository;
import com.eduforge.platform.repository.CourseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagIndexService {

    private static final Logger log = LoggerFactory.getLogger(RagIndexService.class);

    public record RagHit(int rank, double score, int chunkIndex, String excerpt) {}
    private record ChunkTf(int idx, String text, Map<String, Integer> tf) {}

    private final CourseChunkRepository chunks;
    private final CourseRepository courses;
    private final AppProperties props;
    private final GeminiEmbeddingService embeddingService;
    private final ObjectMapper om = new ObjectMapper();

    public RagIndexService(CourseChunkRepository chunks, CourseRepository courses, 
                          AppProperties props, GeminiEmbeddingService embeddingService) {
        this.chunks = chunks;
        this.courses = courses;
        this.props = props;
        this.embeddingService = embeddingService;
    }

    /**
     * Réindexe un cours avec TF-IDF (fallback) ET embeddings Gemini (si disponible).
     */
    @Transactional
    public int reindexCourse(Long courseId, String fullText) {
        if (fullText == null || fullText.isBlank()) {
            throw new IllegalArgumentException("Texte vide : rien à indexer.");
        }

        // Supprime les anciens morceaux (chunks) du cours
        chunks.deleteByCourseId(courseId);

        int chunkChars = Math.max(400, props.getRag().getChunkChars());
        List<String> parts = splitByChars(fullText, chunkChars);

        boolean useEmbeddings = embeddingService.isAvailable();
        log.info("Indexation du cours {} : {} chunks, embeddings={}", courseId, parts.size(), useEmbeddings);

        int i = 0;
        for (String p : parts) {
            Map<String, Integer> tf = termFreq(tokenize(p));
            try {
                String json = om.writeValueAsString(tf);
                CourseChunk chunk = new CourseChunk(courseId, i, p, json);
                
                // Générer l'embedding si le service est disponible
                if (useEmbeddings) {
                    try {
                        float[] embedding = embeddingService.embed(p);
                        chunk.setEmbedding(embedding);
                        log.debug("Embedding généré pour chunk {} du cours {}", i, courseId);
                    } catch (Exception e) {
                        log.warn("Échec embedding pour chunk {}: {}", i, e.getMessage());
                        // On continue sans embedding - le TF-IDF servira de fallback
                    }
                }
                
                chunks.save(chunk); 
            } catch (Exception e) {
                throw new IllegalStateException("Erreur lors de la génération du JSON des termes.", e);
            }
            i++;
        }

        courses.findById(courseId).ifPresent(c -> {
            c.setIndexedAt(Instant.now());
            courses.save(c);
        });
        
        return parts.size();
    }

    /**
     * Recherche sémantique avec embeddings (préféré) ou fallback TF-IDF.
     */
    public List<RagHit> searchTopK(Long courseId, String query) {
        int topK = Math.max(3, props.getRag().getTopK());

        // Vérifier si on peut utiliser les embeddings
        long embeddedCount = chunks.countByCourseIdAndHasEmbeddingTrue(courseId);
        
        if (embeddedCount > 0 && embeddingService.isAvailable()) {
            try {
                return searchWithEmbeddings(courseId, query, topK);
            } catch (Exception e) {
                log.warn("Recherche par embeddings échouée, fallback TF-IDF: {}", e.getMessage());
            }
        }

        // Fallback: recherche TF-IDF classique
        return searchWithTfIdf(courseId, query, topK);
    }

    /**
     * Recherche sémantique via embeddings pgvector.
     */
    private List<RagHit> searchWithEmbeddings(Long courseId, String query, int topK) {
        log.debug("Recherche par embeddings pour cours {}, query: {}", courseId, query);
        
        // Générer l'embedding de la requête
        float[] queryEmbedding = embeddingService.embed(query);
        
        // Convertir en string pour la requête SQL (format pgvector)
        String embeddingStr = floatArrayToVectorString(queryEmbedding);
        
        // Recherche vectorielle
        List<CourseChunk> results = chunks.findSimilarChunks(courseId, embeddingStr, topK);
        
        List<RagHit> hits = new ArrayList<>();
        int r = 1;
        for (CourseChunk c : results) {
            // Score cosinus = 1 - distance (pgvector retourne la distance)
            double score = 1.0; // Score approximatif, pgvector trie par similarité
            hits.add(new RagHit(r++, score, c.getChunkIndex(), excerpt(c.getChunkText(), 420)));
        }
        
        log.debug("Recherche par embeddings: {} résultats", hits.size());
        return hits;
    }

    /**
     * Recherche TF-IDF (fallback).
     */
    private List<RagHit> searchWithTfIdf(Long courseId, String query, int topK) {
        log.debug("Recherche TF-IDF pour cours {}", courseId);
        
        List<CourseChunk> all = chunks.findByCourseIdOrderByChunkIndexAsc(courseId);
        if (all.isEmpty()) return List.of();

        List<String> qTokens = tokenize(query);
        if (qTokens.isEmpty()) return List.of();

        List<ChunkTf> chunkTfs = new ArrayList<>(all.size());
        Map<String, Integer> df = new HashMap<>();

        for (CourseChunk c : all) {
            Map<String, Integer> tf = readTf(c.getTermsJson());
            chunkTfs.add(new ChunkTf(c.getChunkIndex(), c.getChunkText(), tf));
            Set<String> uniq = tf.keySet();
            for (String term : uniq) df.merge(term, 1, Integer::sum);
        }

        int N = chunkTfs.size();
        Map<String, Double> idf = new HashMap<>();
        for (var e : df.entrySet()) {
            double val = Math.log((N + 1.0) / (e.getValue() + 1.0)) + 1.0;
            idf.put(e.getKey(), val);
        }

        Map<String, Integer> qtf = termFreq(qTokens);
        Map<String, Double> qv = tfidf(qtf, idf);

        List<Scored> scored = new ArrayList<>();
        for (ChunkTf c : chunkTfs) {
            Map<String, Double> dv = tfidf(c.tf(), idf);
            double score = cosine(qv, dv);
            if (score > 0) {
                scored.add(new Scored(c.idx(), score, c.text()));
            }
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));
        List<Scored> top = scored.stream().limit(topK).collect(Collectors.toList());

        List<RagHit> hits = new ArrayList<>();
        int r = 1;
        for (Scored s : top) {
            hits.add(new RagHit(r++, s.score, s.idx, excerpt(s.text, 420)));
        }
        return hits;
    }

    /**
     * Convertit un tableau de floats en string format pgvector.
     */
    private String floatArrayToVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Vérifie si un cours a des embeddings.
     */
    public boolean hasEmbeddings(Long courseId) {
        return chunks.countByCourseIdAndHasEmbeddingTrue(courseId) > 0;
    }

    /**
     * Statistiques d'indexation pour un cours.
     */
    public IndexStats getIndexStats(Long courseId) {
        long total = chunks.countByCourseId(courseId);
        long withEmbeddings = chunks.countByCourseIdAndHasEmbeddingTrue(courseId);
        return new IndexStats(total, withEmbeddings, embeddingService.isAvailable());
    }

    public record IndexStats(long totalChunks, long chunksWithEmbeddings, boolean embeddingServiceAvailable) {}

    private record Scored(int idx, double score, String text) {}

    private Map<String, Integer> readTf(String json) {
        try {
            return om.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    private List<String> splitByChars(String text, int maxChars) {
        List<String> parts = new ArrayList<>();
        String t = text.trim().replaceAll("\\r", "");
        int start = 0;
        while (start < t.length()) {
            int end = Math.min(t.length(), start + maxChars);
            int cut = bestCut(t, start, end);
            String chunk = t.substring(start, cut).trim();
            if (!chunk.isBlank()) parts.add(chunk);
            start = cut;
        }
        return parts;
    }

    private int bestCut(String t, int start, int end) {
        int cut = end;
        for (int i = end - 1; i > start + 200; i--) {
            char c = t.charAt(i);
            if (c == '\n' || c == '.' || c == '!' || c == '?') {
                cut = i + 1;
                break;
            }
        }
        return cut;
    }

    private List<String> tokenize(String text) {
        if (text == null) return List.of();
        String norm = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim();
        if (norm.isBlank()) return List.of();

        String[] raw = norm.split("\\s+");
        List<String> tokens = new ArrayList<>(raw.length);
        for (String w : raw) {
            if (w.length() < 2) continue;
            if (STOP.contains(w)) continue;
            tokens.add(w);
        }
        return tokens;
    }

    private Map<String, Integer> termFreq(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1, Integer::sum);
        return tf;
    }

    private Map<String, Double> tfidf(Map<String, Integer> tf, Map<String, Double> idf) {
        Map<String, Double> v = new HashMap<>();
        double norm = 0.0;
        for (var e : tf.entrySet()) {
            double w = (1.0 + Math.log(e.getValue())) * idf.getOrDefault(e.getKey(), 1.0);
            v.put(e.getKey(), w);
            norm += w * w;
        }
        norm = Math.sqrt(Math.max(norm, 1e-12));
        for (var k : new ArrayList<>(v.keySet())) v.put(k, v.get(k) / norm);
        return v;
    }

    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        double s = 0.0;
        Map<String, Double> small = a.size() <= b.size() ? a : b;
        Map<String, Double> large = small == a ? b : a;
        for (var e : small.entrySet()) {
            Double bv = large.get(e.getKey());
            if (bv != null) s += e.getValue() * bv;
        }
        return s;
    }

    private String excerpt(String text, int max) {
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + " …";
    }

    private static final Set<String> STOP = Set.of(
            "le","la","les","un","une","des","du","de","d","et","ou","à","au","aux","en",
            "dans","sur","pour","par","avec","sans","ce","cet","cette","ces","son","sa","ses",
            "est","sont","être","avoir","a","as","ont","il","elle","ils","elles","on","nous","vous",
            "je","tu","se","qui","que","quoi","dont","plus","moins","très","ainsi","comme"
    );
}
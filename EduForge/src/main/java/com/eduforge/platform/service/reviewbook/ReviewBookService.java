package com.eduforge.platform.service.reviewbook;

import com.eduforge.platform.config.AiProperties;
import com.eduforge.platform.domain.reviewbook.*;
import com.eduforge.platform.repository.ReviewBookRepository;
import com.eduforge.platform.service.ai.AiGateway;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@Service
public class ReviewBookService {

    private static final Logger log = LoggerFactory.getLogger(ReviewBookService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "txt", "jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final ReviewBookRepository repository;
    private final AiGateway aiGateway;
    private final AiProperties aiProps;

    @Value("${eduforge.storage.reviewbooks:data/reviewbooks}")
    private String storagePath;

    public ReviewBookService(ReviewBookRepository repository, AiGateway aiGateway, AiProperties aiProps) {
        this.repository = repository;
        this.aiGateway = aiGateway;
        this.aiProps = aiProps;
    }

    public List<ReviewBook> getStudentBooks(Long studentId) {
        return repository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public Optional<ReviewBook> getById(Long id) {
        return repository.findById(id);
    }

    public ReviewBook requireOwned(Long bookId, Long studentId) {
        return repository.findById(bookId)
                .filter(b -> b.getStudentId().equals(studentId))
                .orElseThrow(() -> new IllegalArgumentException("Document introuvable ou non autorisé."));
    }

    @Transactional
    public ReviewBook upload(Long studentId, String title, MultipartFile file) throws IOException {
        // Validation
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 10MB).");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Nom de fichier invalide.");
        }

        String ext = getExtension(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Format non supporté. Formats acceptés: PDF, TXT, JPG, PNG, WEBP.");
        }

        // Déterminer le type
        ReviewBookFileType fileType = switch (ext) {
            case "pdf" -> ReviewBookFileType.PDF;
            case "txt" -> ReviewBookFileType.TXT;
            default -> ReviewBookFileType.IMAGE;
        };

        // Stocker le fichier
        String storedName = UUID.randomUUID() + "." + ext;
        Path studentDir = Paths.get(storagePath, String.valueOf(studentId));
        Files.createDirectories(studentDir);
        Path targetPath = studentDir.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Créer l'entrée
        ReviewBook book = new ReviewBook(studentId, title, originalName, targetPath.toString(), fileType);
        book = repository.save(book);

        // Lancer l'extraction en asynchrone
        processAsync(book.getId());

        return book;
    }

    @Async
    public void processAsync(Long bookId) {
        try {
            process(bookId);
        } catch (Exception e) {
            log.error("Erreur traitement ReviewBook {}: {}", bookId, e.getMessage());
        }
    }

    @Transactional
    public void process(Long bookId) {
        ReviewBook book = repository.findById(bookId).orElse(null);
        if (book == null) return;

        try {
            book.setStatus(ReviewBookStatus.PROCESSING);
            repository.save(book);

            String extractedText = extractText(book);
            
            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException("Aucun texte extrait du document.");
            }

            book.setExtractedText(extractedText);
            book.setStatus(ReviewBookStatus.READY);
            book.setProcessedAt(Instant.now());
            repository.save(book);

            log.info("ReviewBook {} traité avec succès. {} caractères extraits.", bookId, extractedText.length());

        } catch (Exception e) {
            log.error("Échec traitement ReviewBook {}: {}", bookId, e.getMessage());
            book.setStatus(ReviewBookStatus.FAILED);
            book.setErrorMessage(e.getMessage());
            repository.save(book);
        }
    }

    private String extractText(ReviewBook book) throws IOException {
        return switch (book.getFileType()) {
            case TXT -> extractFromTxt(book.getStoredPath());
            case PDF -> extractFromPdf(book.getStoredPath());
            case IMAGE -> extractFromImageWithOcr(book.getStoredPath());
        };
    }

    private String extractFromTxt(String path) throws IOException {
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }

    private String extractFromPdf(String path) throws IOException {
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(Paths.get(path).toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /**
     * Utilise Gemini Vision pour OCR sur les images
     */
    private String extractFromImageWithOcr(String path) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(path));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        
        String mimeType = detectMimeType(path);

        String system = """
            Tu es un système OCR haute précision.
            Extrais TOUT le texte visible dans l'image.
            Conserve la structure (paragraphes, listes, titres).
            Ne résume pas, transcris fidèlement.
            Si l'image contient du texte manuscrit, fais de ton mieux pour le transcrire.
            """;

        String user = "Extrais tout le texte de cette image. Retourne uniquement le texte extrait, sans commentaire.";

        try {
            // Appel à Gemini avec image
            String extracted = aiGateway.generateWithImage(system, user, base64Image, mimeType);
            return extracted != null ? extracted : "";
        } catch (Exception e) {
            log.error("Erreur OCR Gemini: {}", e.getMessage());
            throw new IOException("Erreur lors de l'extraction OCR: " + e.getMessage());
        }
    }

    private String detectMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1) : "";
    }

    @Transactional
    public void delete(Long bookId, Long studentId) {
        ReviewBook book = requireOwned(bookId, studentId);
        
        // Supprimer le fichier
        try {
            Files.deleteIfExists(Paths.get(book.getStoredPath()));
        } catch (IOException e) {
            log.warn("Impossible de supprimer le fichier: {}", e.getMessage());
        }
        
        repository.delete(book);
    }

    /**
     * Génère un cours de révision complet basé sur un prompt utilisateur
     */
    @Transactional
    public ReviewBook generateCourseFromPrompt(Long studentId, String title, String userPrompt) {
        if (userPrompt == null || userPrompt.isBlank()) {
            throw new IllegalArgumentException("Le prompt ne peut pas être vide.");
        }

        String system = """
            Tu es un expert pédagogique et créateur de contenu éducatif.
            Ta tâche : générer un cours de révision complet, structuré et approfondi.
            
            Le cours doit :
            - Être divisé en sections claires (Introduction, Concepts clés, Exemples, Points importants, Résumé)
            - Contenir des explications détaillées et pédagogiques
            - Inclure des exemples concrets et pertinents
            - Utiliser un langage clair et accessible
            - Faire minimum 1500 mots pour un sujet standard
            
            Format attendu : Markdown avec titres ## pour les sections.
            """;

        String user = "Génère un cours de révision complet sur le sujet suivant :\n\n" + userPrompt;

        try {
            String generatedContent = aiGateway.generateJson(system, user);
            
            if (generatedContent == null || generatedContent.isBlank()) {
                throw new IllegalArgumentException("Le contenu généré est vide.");
            }

            // Créer un ReviewBook avec le contenu généré
            ReviewBook book = new ReviewBook(studentId, title, "generated.txt", "AI_GENERATED", ReviewBookFileType.TXT);
            book.setExtractedText(generatedContent);
            book.setStatus(ReviewBookStatus.READY);
            book.setProcessedAt(Instant.now());
            
            return repository.save(book);
            
        } catch (Exception e) {
            log.error("Erreur génération cours depuis prompt: {}", e.getMessage());
            throw new IllegalArgumentException("Échec de la génération: " + e.getMessage());
        }
    }
}

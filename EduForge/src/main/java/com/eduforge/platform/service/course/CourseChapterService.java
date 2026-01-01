package com.eduforge.platform.service.course;

import com.eduforge.platform.domain.course.*;
import com.eduforge.platform.repository.*;
import com.eduforge.platform.service.rag.RagIndexService;
import com.eduforge.platform.service.rag.TextExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class CourseChapterService {

    private final CourseChapterRepository chapters;
    private final CourseRepository courses;
    private final RagIndexService ragIndex;
    private final TextExtractor textExtractor;
    private final Path uploadsRoot;

    public CourseChapterService(CourseChapterRepository chapters,
                                CourseRepository courses,
                                RagIndexService ragIndex,
                                TextExtractor textExtractor,
                                @Value("${app.uploads-dir:data/uploads}") String uploadsDir) {
        this.chapters = chapters;
        this.courses = courses;
        this.ragIndex = ragIndex;
        this.textExtractor = textExtractor;
        this.uploadsRoot = Paths.get(uploadsDir);
    }

    public List<CourseChapter> listByCourse(Long courseId) {
        return chapters.findByCourseIdOrderByChapterOrderAsc(courseId);
    }

    public List<CourseChapter> listPublished(Long courseId) {
        return chapters.findByCourseIdAndIsPublishedTrueOrderByChapterOrderAsc(courseId);
    }

    public Optional<CourseChapter> getById(Long chapterId) {
        return chapters.findById(chapterId);
    }

    @Transactional
    public CourseChapter create(Long courseId, String title, String description) {
        int nextOrder = chapters.getMaxOrderByCourse(courseId).orElse(0) + 1;
        CourseChapter chapter = new CourseChapter(courseId, title, nextOrder);
        chapter.setDescription(description);
        return chapters.save(chapter);
    }

    @Transactional
    public CourseChapter update(Long chapterId, String title, String description, Integer durationMinutes) {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));
        ch.setTitle(title.trim());
        ch.setDescription(description);
        ch.setDurationMinutes(durationMinutes);
        return chapters.save(ch);
    }

    @Transactional
    public CourseChapter uploadContent(Long chapterId, MultipartFile file) throws IOException {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();

        // Déterminer le type de contenu
        String type = "pdf";
        if (contentType != null) {
            if (contentType.contains("pdf")) {
                type = "pdf";
            } else if (contentType.contains("powerpoint") || contentType.contains("presentation") ||
                    (originalName != null && originalName.toLowerCase().endsWith(".pptx"))) {
                type = "pptx";
            } else if (contentType.contains("video")) {
                type = "video";
            } else {
                throw new IllegalArgumentException("Type de fichier non supporté. Utilisez PDF, PPTX ou vidéo.");
            }
        }

        // Créer le dossier
        Path chapterDir = uploadsRoot.resolve("chapters").resolve(chapterId.toString());
        Files.createDirectories(chapterDir);

        // Sauvegarder le fichier
        String fileName = UUID.randomUUID().toString() + "_" + originalName;
        Path filePath = chapterDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        ch.setContentType(type);
        ch.setContentPath(filePath.toString());

        // Indexer pour RAG si c'est un PDF ou PPTX
        if ("pdf".equals(type) || "pptx".equals(type)) {
            try {
                Course course = courses.findById(ch.getCourseId()).orElse(null);
                if (course != null) {
                    String extractedText = textExtractor.extract(filePath.toString());
                    if (extractedText != null && !extractedText.isBlank()) {
                        ragIndex.reindexCourse(course.getId(), extractedText);
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur d'indexation RAG: " + e.getMessage());
            }
        }

        return chapters.save(ch);
    }

    @Transactional
    public CourseChapter setTextContent(Long chapterId, String textContent) {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));
        ch.setContentType("text");
        ch.setTextContent(textContent);
        return chapters.save(ch);
    }

    @Transactional
    public CourseChapter setVideoUrl(Long chapterId, String videoUrl) {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));
        ch.setContentType("video");
        ch.setVideoUrl(videoUrl);
        return chapters.save(ch);
    }

    @Transactional
    public CourseChapter publish(Long chapterId) {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));
        ch.setIsPublished(true);
        return chapters.save(ch);
    }

    @Transactional
    public CourseChapter unpublish(Long chapterId) {
        CourseChapter ch = chapters.findById(chapterId)
                .orElseThrow(() -> new IllegalArgumentException("Chapitre introuvable."));
        ch.setIsPublished(false);
        return chapters.save(ch);
    }

    @Transactional
    public void reorder(Long courseId, List<Long> chapterIds) {
        int order = 1;
        for (Long id : chapterIds) {
            final int currentOrder = order++;
            chapters.findById(id)
                    .filter(ch -> ch.getCourseId().equals(courseId))
                    .ifPresent(ch -> {
                        ch.setChapterOrder(currentOrder);
                        chapters.save(ch);
                    });
        }
    }

    @Transactional
    public void delete(Long chapterId) {
        CourseChapter ch = chapters.findById(chapterId).orElse(null);
        if (ch != null) {
            if (ch.getContentPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(ch.getContentPath()));
                } catch (IOException e) {
                    // Log mais continuer
                }
            }
            chapters.delete(ch);
        }
    }

    public Path getFilePath(Long chapterId) {
        return chapters.findById(chapterId)
                .filter(ch -> ch.getContentPath() != null)
                .map(ch -> Paths.get(ch.getContentPath()))
                .filter(Files::exists)
                .orElse(null);
    }

    public int getPublishedCount(Long courseId) {
        return chapters.findByCourseIdAndIsPublishedTrueOrderByChapterOrderAsc(courseId).size();
    }
}

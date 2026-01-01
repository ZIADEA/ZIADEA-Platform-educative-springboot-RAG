package com.eduforge.platform.service.library;

import com.eduforge.platform.domain.library.LibraryResource;
import com.eduforge.platform.repository.LibraryResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LibraryService {

    private final LibraryResourceRepository resources;
    private final Path uploadsRoot;

    public LibraryService(LibraryResourceRepository resources, Path uploadsRoot) {
        this.resources = resources;
        this.uploadsRoot = uploadsRoot;
    }

    public List<LibraryResource> listByInstitution(Long institutionId) {
        return resources.findByInstitutionIdOrderByCreatedAtDesc(institutionId);
    }

    public List<LibraryResource> listByCategory(Long institutionId, String category) {
        if (category == null || category.isBlank()) {
            return listByInstitution(institutionId);
        }
        return resources.findByInstitutionIdAndCategoryOrderByCreatedAtDesc(institutionId, category);
    }

    public List<LibraryResource> search(Long institutionId, String query) {
        if (query == null || query.isBlank()) {
            return listByInstitution(institutionId);
        }
        return resources.findByInstitutionIdAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                institutionId, query.trim());
    }

    public Optional<LibraryResource> getById(Long id) {
        return resources.findById(id);
    }

    @Transactional
    public LibraryResource upload(Long institutionId, Long uploaderId, MultipartFile file,
                                   String title, String description, String category,
                                   String author, String isbn, Integer publishedYear) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide.");
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("document.pdf");
        String ext = extensionOf(originalName).toLowerCase(Locale.ROOT);

        if (!ext.equals("pdf") && !ext.equals("txt")) {
            throw new IllegalArgumentException("Seuls les fichiers PDF et TXT sont acceptés pour la bibliothèque.");
        }

        String fileType = ext.toUpperCase(Locale.ROOT);
        Path libDir = uploadsRoot.resolve("library").resolve(String.valueOf(institutionId));
        try {
            Files.createDirectories(libDir);
            String safeName = System.currentTimeMillis() + "-" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path stored = libDir.resolve(safeName);
            Files.copy(file.getInputStream(), stored);

            LibraryResource resource = new LibraryResource(
                    institutionId,
                    title != null && !title.isBlank() ? title.trim() : originalName,
                    originalName,
                    stored.toString(),
                    fileType,
                    uploaderId
            );

            resource.setFileSize(file.getSize());
            resource.setDescription(description);
            resource.setCategory(category);
            resource.setAuthor(author);
            resource.setIsbn(isbn);
            resource.setPublishedYear(publishedYear);

            return resources.save(resource);
        } catch (IOException e) {
            throw new IllegalArgumentException("Erreur lors de l'upload du fichier.", e);
        }
    }

    @Transactional
    public LibraryResource update(Long resourceId, String title, String description, 
                                   String category, String author, String isbn, 
                                   Integer publishedYear, Boolean isPublic) {
        LibraryResource resource = resources.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource introuvable."));

        if (title != null && !title.isBlank()) {
            resource.setTitle(title.trim());
        }
        resource.setDescription(description);
        resource.setCategory(category);
        resource.setAuthor(author);
        resource.setIsbn(isbn);
        resource.setPublishedYear(publishedYear);
        if (isPublic != null) {
            resource.setIsPublic(isPublic);
        }

        return resources.save(resource);
    }

    @Transactional
    public void delete(Long resourceId) {
        LibraryResource resource = resources.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource introuvable."));
        
        // Supprimer le fichier physique
        try {
            Files.deleteIfExists(Path.of(resource.getStoredPath()));
        } catch (IOException ignored) {}

        resources.delete(resource);
    }

    @Transactional
    public void incrementDownload(Long resourceId) {
        resources.findById(resourceId).ifPresent(r -> {
            r.incrementDownloads();
            resources.save(r);
        });
    }

    public Path getFilePath(Long resourceId) {
        LibraryResource resource = resources.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Ressource introuvable."));
        return Path.of(resource.getStoredPath());
    }

    private String extensionOf(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) return "";
        return name.substring(i + 1);
    }
}

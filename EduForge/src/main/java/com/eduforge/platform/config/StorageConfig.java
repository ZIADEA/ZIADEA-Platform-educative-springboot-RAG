package com.eduforge.platform.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public Path uploadsRoot(AppProperties props) {
        try {
            Path root = Path.of(props.getStorage().getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'initialiser le dossier d'upload", e);
        }
    }
}

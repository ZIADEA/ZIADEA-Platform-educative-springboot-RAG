package com.eduforge.platform.service.rag;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extracteur de texte pour différents formats de fichiers (PDF, PPTX)
 */
@Component
public class TextExtractor {

    /**
     * Extrait le texte d'un fichier selon son extension
     */
    public String extract(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String fileName = path.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            return extractFromPdf(path);
        } else if (fileName.endsWith(".pptx")) {
            return extractFromPptx(path);
        } else if (fileName.endsWith(".txt")) {
            return extractFromText(path);
        } else {
            throw new IllegalArgumentException("Format de fichier non supporté: " + fileName);
        }
    }

    /**
     * Extrait le texte d'un fichier PDF avec Apache PDFBox 3.x
     */
    public String extractFromPdf(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * Extrait le texte d'un fichier PowerPoint avec Apache POI
     */
    public String extractFromPptx(Path filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XMLSlideShow pptx = new XMLSlideShow(fis)) {
            
            int slideNum = 1;
            for (XSLFSlide slide : pptx.getSlides()) {
                sb.append("\n=== Slide ").append(slideNum++).append(" ===\n");
                
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text).append("\n");
                        }
                    }
                }
            }
        }
        
        return sb.toString().trim();
    }

    /**
     * Lit le contenu d'un fichier texte brut
     */
    public String extractFromText(Path filePath) throws IOException {
        return Files.readString(filePath);
    }
}

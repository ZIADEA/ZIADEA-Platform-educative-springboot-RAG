package com.eduforge.platform.service.course;

import org.apache.pdfbox.Loader; // Import modifié
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class TextExtractionService {

    /**
     * Extrait le texte d'un fichier PDF (Compatible PDFBox 3.0+)
     */
    public String extractPdf(InputStream in) {
        // En PDFBox 3.0, on utilise Loader.loadPDF(in) au lieu de PDDocument.load(in)
        try (PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return normalize(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible d'extraire le texte du PDF.", e);
        }
    }

    /**
     * Extrait le texte d'un fichier PowerPoint (.pptx)
     */
    public String extractPptx(InputStream in) {
        try (XMLSlideShow ppt = new XMLSlideShow(in)) {
            StringBuilder sb = new StringBuilder();
            ppt.getSlides().forEach(slide -> {
                slide.getShapes().forEach(shape -> appendShapeText(shape, sb));
                sb.append("\n");
            });
            return normalize(sb.toString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible d'extraire le texte du PPTX.", e);
        }
    }

    /**
     * Extrait le texte d'un fichier brut (.txt)
     */
    public String extractPlainText(InputStream in) {
        try {
            byte[] bytes = in.readAllBytes();
            return normalize(new String(bytes, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible de lire le texte.", e);
        }
    }

    private void appendShapeText(XSLFShape shape, StringBuilder sb) {
        if (shape instanceof XSLFTextShape t) {
            String tx = t.getText();
            if (tx != null && !tx.isBlank()) {
                sb.append(tx).append("\n");
            }
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        // Nettoyage des caractères nuls et normalisation des espaces/sauts de ligne
        return s.replace("\u0000", " ")
                .replaceAll("[\\t\\r]+", " ")
                .replaceAll(" +", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
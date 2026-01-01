package com.eduforge.platform.service.certificate;

import com.eduforge.platform.domain.certificate.Certificate;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.repository.*;
import com.eduforge.platform.service.messaging.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CertificateService {

    private final CertificateRepository certificates;
    private final CourseRepository courses;
    private final UserRepository users;
    private final NotificationService notifications;
    private final Path uploadsRoot;

    public CertificateService(CertificateRepository certificates,
                              CourseRepository courses,
                              UserRepository users,
                              NotificationService notifications,
                              @Value("${app.uploads-dir:data/uploads}") String uploadsDir) {
        this.certificates = certificates;
        this.courses = courses;
        this.users = users;
        this.notifications = notifications;
        this.uploadsRoot = Paths.get(uploadsDir);
    }

    public List<Certificate> getStudentCertificates(Long studentId) {
        return certificates.findByStudentIdOrderByIssuedAtDesc(studentId);
    }

    public Optional<Certificate> getById(Long certificateId) {
        return certificates.findById(certificateId);
    }

    public Optional<Certificate> getByCode(String code) {
        return certificates.findByCertificateCode(code);
    }

    public boolean hasCertificate(Long courseId, Long studentId) {
        return certificates.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public Certificate issue(Long courseId, Long studentId, int finalScore) {
        // Vérifier si déjà émis
        if (certificates.existsByStudentIdAndCourseId(studentId, courseId)) {
            return certificates.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                    .filter(c -> courseId.equals(c.getCourseId()))
                    .findFirst()
                    .orElse(null);
        }

        Course course = courses.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable."));
        User student = users.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable."));

        Certificate cert = new Certificate(studentId, course.getTitle(), "COMPLETION");
        cert.setCourseId(courseId);
        cert.setFinalScore(BigDecimal.valueOf(finalScore));
        cert.setDescription("Certificat de complétion du cours: " + course.getTitle());

        Certificate saved = certificates.save(cert);

        // Générer le HTML du certificat
        try {
            String htmlPath = generateHtml(saved, student, course);
            saved.setPdfPath(htmlPath);
            saved = certificates.save(saved);
        } catch (Exception e) {
            System.err.println("Erreur génération certificat: " + e.getMessage());
        }

        // Notification
        notifications.notifyCertificateEarned(studentId, course.getTitle(), saved.getId());

        return saved;
    }

    public boolean verify(String certificateCode) {
        return certificates.findByCertificateCode(certificateCode).isPresent();
    }

    public Optional<Certificate> verifyCertificate(String code) {
        return certificates.findByCertificateCode(code);
    }

    private String generateHtml(Certificate cert, User student, Course course) throws IOException {
        // Créer le répertoire
        Path certDir = uploadsRoot.resolve("certificates");
        Files.createDirectories(certDir);

        String fileName = "cert_" + cert.getCertificateCode().replace("-", "") + ".html";
        Path htmlPath = certDir.resolve(fileName);

        // Générer le HTML
        String html = generateCertificateHtml(cert, student, course);
        Files.writeString(htmlPath, html);

        return htmlPath.toString();
    }

    private String generateCertificateHtml(Certificate cert, User student, Course course) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy")
                .withZone(ZoneId.of("Europe/Paris"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Certificat - %s</title>
                <style>
                    body {
                        font-family: 'Georgia', serif;
                        margin: 0;
                        padding: 40px;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        min-height: 100vh;
                    }
                    .certificate {
                        background: white;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 60px;
                        border: 3px solid #d4af37;
                        box-shadow: 0 10px 50px rgba(0,0,0,0.3);
                        text-align: center;
                    }
                    .header {
                        color: #1a1a2e;
                        font-size: 14px;
                        text-transform: uppercase;
                        letter-spacing: 3px;
                        margin-bottom: 20px;
                    }
                    .title {
                        font-size: 42px;
                        color: #d4af37;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .subtitle {
                        font-size: 18px;
                        color: #555;
                        margin-bottom: 30px;
                    }
                    .recipient {
                        font-size: 32px;
                        color: #1a1a2e;
                        margin: 30px 0;
                        font-style: italic;
                    }
                    .course-title {
                        font-size: 24px;
                        color: #667eea;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .score {
                        font-size: 18px;
                        color: #333;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 50px;
                        display: flex;
                        justify-content: space-between;
                        align-items: flex-end;
                    }
                    .date, .verification {
                        font-size: 12px;
                        color: #888;
                    }
                    .verification {
                        font-family: monospace;
                        background: #f5f5f5;
                        padding: 5px 10px;
                        border-radius: 4px;
                    }
                    .seal {
                        width: 100px;
                        height: 100px;
                        border: 3px solid #d4af37;
                        border-radius: 50%%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 12px;
                        color: #d4af37;
                        text-transform: uppercase;
                    }
                </style>
            </head>
            <body>
                <div class="certificate">
                    <div class="header">EduForge - Plateforme Éducative</div>
                    <div class="title">CERTIFICAT</div>
                    <div class="subtitle">de Réussite</div>
                    
                    <p>Nous certifions que</p>
                    <div class="recipient">%s</div>
                    
                    <p>a complété avec succès le cours</p>
                    <div class="course-title">%s</div>
                    
                    <div class="score">Score final : %s%%</div>
                    
                    <div class="footer">
                        <div class="date">
                            Délivré le %s
                        </div>
                        <div class="seal">
                            EduForge<br>Certifié
                        </div>
                        <div class="verification">
                            Code: %s
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                course.getTitle(),
                student.getFullName(),
                course.getTitle(),
                cert.getFinalScore() != null ? cert.getFinalScore().intValue() : 100,
                fmt.format(cert.getIssuedAt()),
                cert.getCertificateCode()
        );
    }

    public Path getHtmlPath(Long certificateId) {
        return certificates.findById(certificateId)
                .filter(c -> c.getPdfPath() != null)
                .map(c -> Paths.get(c.getPdfPath()))
                .filter(Files::exists)
                .orElse(null);
    }
}

package com.eduforge.platform.service.classroom;

import com.eduforge.platform.domain.classroom.*;
import com.eduforge.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class StudentProgressService {

    private final StudentProgressRepository progressRepo;
    private final ClassroomLevelRepository levels;
    private final ClassroomRepository classrooms;

    public StudentProgressService(StudentProgressRepository progressRepo,
                                  ClassroomLevelRepository levels,
                                  ClassroomRepository classrooms) {
        this.progressRepo = progressRepo;
        this.levels = levels;
        this.classrooms = classrooms;
    }

    public Optional<StudentProgress> getProgress(Long classroomId, Long studentId) {
        return progressRepo.findByClassroomIdAndStudentId(classroomId, studentId);
    }

    public List<StudentProgress> getClassroomProgress(Long classroomId) {
        return progressRepo.findByClassroomIdOrderByXpPointsDesc(classroomId);
    }

    public List<StudentProgress> getStudentProgress(Long studentId) {
        return progressRepo.findByStudentIdOrderByUpdatedAtDesc(studentId);
    }

    public List<StudentProgress> getTopStudents(Long classroomId, int limit) {
        return progressRepo.findTopByClassroom(classroomId, limit);
    }

    @Transactional
    public StudentProgress initProgress(Long classroomId, Long studentId) {
        Optional<StudentProgress> existing = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        StudentProgress progress = new StudentProgress(classroomId, studentId);
        
        // Assigner le niveau initial (niveau 1)
        List<ClassroomLevel> classroomLevels = levels.findByClassroomIdOrderByLevelNumberAsc(classroomId);
        if (!classroomLevels.isEmpty()) {
            progress.setCurrentLevelId(classroomLevels.get(0).getId());
        }

        return progressRepo.save(progress);
    }

    @Transactional
    public StudentProgress addXp(Long classroomId, Long studentId, int xpAmount, String activity) {
        StudentProgress progress = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseGet(() -> initProgress(classroomId, studentId));

        progress.setXpPoints(progress.getXpPoints() + xpAmount);
        progress.setTotalActivities(progress.getTotalActivities() + 1);
        progress.setLastActivityAt(Instant.now());

        // Vérifier le passage de niveau
        checkLevelUp(progress);

        return progressRepo.save(progress);
    }

    @Transactional
    public StudentProgress recordQuizAttempt(Long classroomId, Long studentId, int score, boolean passed) {
        StudentProgress progress = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseGet(() -> initProgress(classroomId, studentId));

        progress.setQuizzesAttempted(progress.getQuizzesAttempted() + 1);
        
        // Calculer la nouvelle moyenne
        double newAvg = ((progress.getAverageScore() * (progress.getQuizzesAttempted() - 1)) + score)
                / progress.getQuizzesAttempted();
        progress.setAverageScore(newAvg);

        if (passed) {
            progress.setQuizzesPassed(progress.getQuizzesPassed() + 1);
        }

        // XP basé sur le score
        int xp = score / 10; // 10% du score comme XP
        if (passed) xp += 5; // Bonus pour réussite
        progress.setXpPoints(progress.getXpPoints() + xp);

        progress.setLastActivityAt(Instant.now());
        checkLevelUp(progress);

        return progressRepo.save(progress);
    }

    @Transactional
    public StudentProgress recordExamAttempt(Long classroomId, Long studentId, int score, boolean passed) {
        StudentProgress progress = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseGet(() -> initProgress(classroomId, studentId));

        progress.setExamsAttempted(progress.getExamsAttempted() + 1);

        if (passed) {
            progress.setExamsPassed(progress.getExamsPassed() + 1);
        }

        // XP pour examen (plus important que quiz)
        int xp = score / 5; // 20% du score comme XP
        if (passed) xp += 20; // Bonus plus important pour réussite examen
        progress.setXpPoints(progress.getXpPoints() + xp);

        progress.setLastActivityAt(Instant.now());
        checkLevelUp(progress);

        return progressRepo.save(progress);
    }

    @Transactional
    public StudentProgress recordAssignment(Long classroomId, Long studentId, int grade, boolean submitted) {
        StudentProgress progress = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseGet(() -> initProgress(classroomId, studentId));

        if (submitted) {
            progress.setAssignmentsSubmitted(progress.getAssignmentsSubmitted() + 1);
            
            // XP pour devoir
            int xp = 10 + (grade / 10); // Base + bonus selon note
            progress.setXpPoints(progress.getXpPoints() + xp);
        }

        progress.setLastActivityAt(Instant.now());
        checkLevelUp(progress);

        return progressRepo.save(progress);
    }

    @Transactional
    public StudentProgress addBadge(Long classroomId, Long studentId, String badge) {
        StudentProgress progress = progressRepo.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseGet(() -> initProgress(classroomId, studentId));

        Set<String> badges = progress.getBadges();
        badges.add(badge);
        progress.setBadges(badges);

        // XP pour badge
        progress.setXpPoints(progress.getXpPoints() + 25);
        progress.setLastActivityAt(Instant.now());

        return progressRepo.save(progress);
    }

    /**
     * Vérifier si l'étudiant peut passer au niveau suivant
     * Basé sur le score moyen et le unlockThreshold du niveau suivant
     */
    private void checkLevelUp(StudentProgress progress) {
        List<ClassroomLevel> classroomLevels = levels.findByClassroomIdOrderByLevelNumberAsc(progress.getClassroomId());
        
        if (classroomLevels.isEmpty()) return;
        
        // Trouver le niveau actuel
        ClassroomLevel currentLevel = null;
        int currentIndex = 0;
        for (int i = 0; i < classroomLevels.size(); i++) {
            if (classroomLevels.get(i).getId().equals(progress.getCurrentLevelId())) {
                currentLevel = classroomLevels.get(i);
                currentIndex = i;
                break;
            }
        }
        
        // Vérifier si on peut passer au niveau suivant
        if (currentIndex < classroomLevels.size() - 1) {
            ClassroomLevel nextLevel = classroomLevels.get(currentIndex + 1);
            // Passage de niveau si XP suffisants et moyenne >= unlockThreshold
            if (progress.getXpPoints() >= (nextLevel.getLevelNumber() * 100) 
                && progress.getAverageScore() >= nextLevel.getUnlockThreshold()) {
                progress.setCurrentLevelId(nextLevel.getId());
            }
        }
    }

    // --- Niveaux de classe ---

    public List<ClassroomLevel> getLevels(Long classroomId) {
        return levels.findByClassroomIdOrderByLevelNumberAsc(classroomId);
    }

    @Transactional
    public ClassroomLevel createLevel(Long classroomId, Integer levelNumber, String name, 
                                      String description, Integer requiredScore, Integer unlockThreshold) {
        ClassroomLevel level = new ClassroomLevel(classroomId, levelNumber, name);
        level.setDescription(description);
        level.setRequiredScore(requiredScore);
        level.setUnlockThreshold(unlockThreshold);
        return levels.save(level);
    }

    @Transactional
    public void initDefaultLevels(Long classroomId) {
        if (levels.findByClassroomIdOrderByLevelNumberAsc(classroomId).isEmpty()) {
            createLevel(classroomId, 1, "Débutant", "Niveau de départ", 50, 50);
            createLevel(classroomId, 2, "Apprenti", "Premiers pas", 60, 60);
            createLevel(classroomId, 3, "Confirmé", "En progression", 70, 70);
            createLevel(classroomId, 4, "Avancé", "Bon niveau", 80, 80);
            createLevel(classroomId, 5, "Expert", "Maîtrise", 90, 90);
        }
    }

    @Transactional
    public void deleteLevel(Long levelId) {
        levels.deleteById(levelId);
    }

    // --- Statistiques ---

    public record ProgressStats(
            int totalStudents,
            double avgXp,
            double avgQuizScore,
            int totalQuizzes,
            int totalExams
    ) {}

    public ProgressStats getClassroomStats(Long classroomId) {
        List<StudentProgress> all = progressRepo.findByClassroomIdOrderByXpPointsDesc(classroomId);
        
        if (all.isEmpty()) {
            return new ProgressStats(0, 0, 0, 0, 0);
        }

        double avgXp = all.stream().mapToInt(StudentProgress::getXpPoints).average().orElse(0);
        double avgScore = all.stream().mapToDouble(StudentProgress::getAverageScore).average().orElse(0);
        int totalQuizzes = all.stream().mapToInt(StudentProgress::getQuizzesAttempted).sum();
        int totalExams = all.stream().mapToInt(StudentProgress::getExamsAttempted).sum();

        return new ProgressStats(all.size(), avgXp, avgScore, totalQuizzes, totalExams);
    }
}

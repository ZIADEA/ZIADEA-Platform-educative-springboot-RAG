package com.eduforge.platform.web.controller;

import com.eduforge.platform.domain.auth.User;
import com.eduforge.platform.domain.classroom.Classroom;
import com.eduforge.platform.domain.classroom.ClassroomEnrollment;
import com.eduforge.platform.domain.course.Course;
import com.eduforge.platform.domain.exam.Exam;
import com.eduforge.platform.domain.exam.ExamAttempt;
import com.eduforge.platform.domain.quiz.Quiz;
import com.eduforge.platform.domain.quiz.QuizAttempt;
import com.eduforge.platform.repository.*;
import com.eduforge.platform.service.classroom.ClassroomService;
import com.eduforge.platform.service.exam.ExamService;
import com.eduforge.platform.util.SecurityUtil;
import com.eduforge.platform.web.dto.ExamAttemptDTO;
import com.eduforge.platform.web.dto.QuizAttemptDTO;
import com.eduforge.platform.web.dto.forms.JoinClassForm;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final ClassroomService classroomService;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ExamService examService;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;

    public StudentController(ClassroomService classroomService,
                             ClassroomEnrollmentRepository enrollmentRepository,
                             ClassroomRepository classroomRepository,
                             QuizAttemptRepository quizAttemptRepository,
                             ExamService examService,
                             UserRepository userRepository,
                             InstitutionRepository institutionRepository,
                             ExamAttemptRepository examAttemptRepository,
                             ExamRepository examRepository,
                             QuizRepository quizRepository,
                             CourseRepository courseRepository) {
        this.classroomService = classroomService;
        this.enrollmentRepository = enrollmentRepository;
        this.classroomRepository = classroomRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.examService = examService;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
        this.quizRepository = quizRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        Long studentId = SecurityUtil.userId(auth);
        model.addAttribute("pageTitle", "Étudiant — Tableau de bord");
        model.addAttribute("classesCount", enrollmentRepository.countByStudentId(studentId));
        model.addAttribute("quizAttempts", quizAttemptRepository.countByStudentId(studentId));
        model.addAttribute("enrolledCoursesCount", 0); // À implémenter selon le modèle de données
        return "student/dashboard";
    }

    @GetMapping("/classes")
    public String classes(Authentication auth, Model model) {
        Long studentId = SecurityUtil.userId(auth);
        List<ClassroomEnrollment> enrollments = classroomService.studentEnrollments(studentId);
        
        // Charger les détails des classes
        List<Long> classroomIds = enrollments.stream()
                .map(ClassroomEnrollment::getClassroomId)
                .collect(Collectors.toList());
        
        Map<Long, Classroom> classroomsMap = new HashMap<>();
        if (!classroomIds.isEmpty()) {
            classroomRepository.findAllById(classroomIds).forEach(c -> classroomsMap.put(c.getId(), c));
        }
        
        model.addAttribute("pageTitle", "Étudiant — Mes classes");
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("classroomsMap", classroomsMap);
        model.addAttribute("classroomService", classroomService);
        model.addAttribute("joinForm", new JoinClassForm());
        return "student/classes";
    }

    @GetMapping("/classes/{classroomId}")
    public String viewClass(Authentication auth,
                           @PathVariable Long classroomId,
                           Model model) {
        Long studentId = SecurityUtil.userId(auth);
        
        // Vérifier que l'étudiant est bien inscrit
        enrollmentRepository.findByClassroomIdAndStudentId(classroomId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Tu n'es pas inscrit à cette classe."));
        
        Classroom classroom = classroomService.getById(classroomId);
        
        model.addAttribute("pageTitle", "Classe — " + classroom.getTitle());
        model.addAttribute("classroom", classroom);
        model.addAttribute("posts", classroomService.getPosts(classroomId));
        model.addAttribute("courses", classroomService.getClassroomCourses(classroomId));
        model.addAttribute("exams", examService.listAvailableExams(classroomId));
        model.addAttribute("studentCount", classroomService.countStudents(classroomId));
        model.addAttribute("courseCount", classroomService.countCourses(classroomId));
        model.addAttribute("owner", classroomService.getOwner(classroom));
        
        return "student/classroom_view";
    }

    @PostMapping("/classes/join")
    public String join(Authentication auth,
                       @Valid @ModelAttribute("joinForm") JoinClassForm form,
                       BindingResult br,
                       Model model) {
        Long studentId = SecurityUtil.userId(auth);

        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Étudiant — Mes classes");
            model.addAttribute("enrollments", classroomService.studentEnrollments(studentId));
            return "student/classes";
        }
        try {
            classroomService.joinByCode(studentId, form.getCode());
            return "redirect:/student/classes";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("pageTitle", "Étudiant — Mes classes");
            model.addAttribute("enrollments", classroomService.studentEnrollments(studentId));
            model.addAttribute("formError", ex.getMessage());
            return "student/classes";
        }
    }

    // Page profil d'un étudiant (accessible par prof dans sa classe)
    @GetMapping("/{studentId}/profile")
    public String studentProfile(Authentication auth,
                                  @PathVariable Long studentId,
                                  @RequestParam(required = false) Long classroomId,
                                  Model model) {
        // Charger l'étudiant
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Étudiant introuvable."));
        
        // Charger nom institution si affilié
        if (student.getInstitutionId() != null) {
            institutionRepository.findById(student.getInstitutionId())
                .ifPresent(inst -> student.setInstitutionName(inst.getName()));
        }
        
        // Statistiques
        long examsAttempted = examAttemptRepository.countByStudentId(studentId);
        long quizzesAttempted = quizAttemptRepository.countByStudentId(studentId);
        long coursesEnrolled = enrollmentRepository.countByStudentId(studentId);
        
        // Historique examens - convertir en DTOs avec titres
        List<ExamAttempt> rawExamAttempts = examAttemptRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
        List<Long> examIds = rawExamAttempts.stream().map(ExamAttempt::getExamId).distinct().collect(Collectors.toList());
        Map<Long, Exam> examsMap = new HashMap<>();
        if (!examIds.isEmpty()) {
            examRepository.findAllById(examIds).forEach(e -> examsMap.put(e.getId(), e));
        }
        List<ExamAttemptDTO> examAttempts = rawExamAttempts.stream().map(a -> {
            Exam exam = examsMap.get(a.getExamId());
            String title = exam != null ? exam.getTitle() : "Examen #" + a.getExamId();
            int passThreshold = exam != null && exam.getPassThreshold() != null ? exam.getPassThreshold() : 50;
            Integer score = a.getScorePercent();
            boolean passed = score != null && score >= passThreshold;
            return new ExamAttemptDTO(a.getId(), title, a.getSubmittedAt(), score, passed, a.getStatus().name());
        }).collect(Collectors.toList());
        
        // Historique quiz - convertir en DTOs avec titres
        List<QuizAttempt> rawQuizAttempts = quizAttemptRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        List<Long> quizIds = rawQuizAttempts.stream().map(QuizAttempt::getQuizId).distinct().collect(Collectors.toList());
        List<Long> courseIds = rawQuizAttempts.stream().map(QuizAttempt::getCourseId).distinct().collect(Collectors.toList());
        Map<Long, Quiz> quizzesMap = new HashMap<>();
        Map<Long, Course> coursesMap = new HashMap<>();
        if (!quizIds.isEmpty()) {
            quizRepository.findAllById(quizIds).forEach(q -> quizzesMap.put(q.getId(), q));
        }
        if (!courseIds.isEmpty()) {
            courseRepository.findAllById(courseIds).forEach(c -> coursesMap.put(c.getId(), c));
        }
        List<QuizAttemptDTO> quizAttempts = rawQuizAttempts.stream().map(a -> {
            Quiz quiz = quizzesMap.get(a.getQuizId());
            Course course = coursesMap.get(a.getCourseId());
            String difficulty = quiz != null ? quiz.getDifficulty().name() : "MEDIUM";
            String courseTitle = course != null ? course.getTitle() : "Cours #" + a.getCourseId();
            String title = courseTitle + " (" + difficulty + ")";
            return new QuizAttemptDTO(a.getId(), title, a.getCreatedAt(), a.getCorrectCount(), a.getTotalCount(), a.getScorePercent());
        }).collect(Collectors.toList());
        
        model.addAttribute("pageTitle", "Profil — " + student.getFullName());
        model.addAttribute("student", student);
        model.addAttribute("examsAttempted", examsAttempted);
        model.addAttribute("quizzesAttempted", quizzesAttempted);
        model.addAttribute("coursesEnrolled", coursesEnrolled);
        model.addAttribute("examAttempts", examAttempts);
        model.addAttribute("quizAttempts", quizAttempts);
        
        // Si vient d'une classe, ajouter info classe
        if (classroomId != null) {
            classroomRepository.findById(classroomId)
                .ifPresent(classroom -> model.addAttribute("classroom", classroom));
        }
        
        return "student/profile";
    }
}

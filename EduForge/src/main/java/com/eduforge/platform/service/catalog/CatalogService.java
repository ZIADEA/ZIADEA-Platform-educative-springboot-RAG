package com.eduforge.platform.service.catalog;

import com.eduforge.platform.domain.catalog.AcademicLevel;
import com.eduforge.platform.domain.catalog.Program;
import com.eduforge.platform.domain.catalog.Subject;
import com.eduforge.platform.repository.AcademicLevelRepository;
import com.eduforge.platform.repository.ProgramRepository;
import com.eduforge.platform.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    private final ProgramRepository programs;
    private final AcademicLevelRepository levels;
    private final SubjectRepository subjects;

    public CatalogService(ProgramRepository programs, AcademicLevelRepository levels, SubjectRepository subjects) {
        this.programs = programs;
        this.levels = levels;
        this.subjects = subjects;
    }

    public List<Program> programs(Long institutionId) {
        return programs.findByInstitutionIdOrderByNameAsc(institutionId);
    }

    public List<AcademicLevel> levels(Long programId) {
        return levels.findByProgramIdOrderByLabelAsc(programId);
    }

    public List<Subject> subjects(Long levelId) {
        return subjects.findByLevelIdOrderByNameAsc(levelId);
    }

    /**
     * Récupère toutes les matières de tous les programmes/niveaux d'une institution
     */
    public List<Subject> allSubjects(Long institutionId) {
        List<Subject> allSubjects = new ArrayList<>();
        for (Program p : programs.findByInstitutionIdOrderByNameAsc(institutionId)) {
            for (AcademicLevel l : levels.findByProgramIdOrderByLabelAsc(p.getId())) {
                allSubjects.addAll(subjects.findByLevelIdOrderByNameAsc(l.getId()));
            }
        }
        return allSubjects;
    }

    @Transactional
    public Program addProgram(Long institutionId, String name) {
        return programs.save(new Program(institutionId, name.trim()));
    }

    @Transactional
    public AcademicLevel addLevel(Long programId, String label) {
        return levels.save(new AcademicLevel(programId, label.trim()));
    }

    @Transactional
    public Subject addSubject(Long levelId, String name) {
        return subjects.save(new Subject(levelId, name.trim()));
    }
    
    public java.util.Optional<Subject> getSubjectById(Long subjectId) {
        return subjects.findById(subjectId);
    }
}

package com.eduforge.platform.domain.classroom;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "classroom_enrollment",
        uniqueConstraints = @UniqueConstraint(name = "uq_class_student", columnNames = {"classroom_id", "student_id"}))
public class ClassroomEnrollment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();

    public ClassroomEnrollment() {}

    public ClassroomEnrollment(Long classroomId, Long studentId) {
        this.classroomId = classroomId;
        this.studentId = studentId;
        this.joinedAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}

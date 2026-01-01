package com.eduforge.platform.domain.course;

import jakarta.persistence.*;

@Entity
@Table(
    name = "classroom_course",
    uniqueConstraints = @UniqueConstraint(name = "uq_class_course", columnNames = {"classroom_id", "course_id"})
)
public class ClassroomCourse {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    public ClassroomCourse() {}

    public ClassroomCourse(Long classroomId, Long courseId) {
        this.classroomId = classroomId;
        this.courseId = courseId;
    }

    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}

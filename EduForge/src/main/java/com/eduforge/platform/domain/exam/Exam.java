package com.eduforge.platform.domain.exam;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Examen programmé par un professeur pour une classe
 */
@Entity
@Table(name = "exam", indexes = {
    @Index(name = "idx_exam_class", columnList = "classroom_id"),
    @Index(name = "idx_exam_status", columnList = "status"),
    @Index(name = "idx_exam_schedule", columnList = "scheduled_start, scheduled_end")
})
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "course_id")
    private Long courseId;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 30)
    private ExamType examType = ExamType.QUIZ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExamStatus status = ExamStatus.DRAFT;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 10;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(name = "pass_threshold")
    private Integer passThreshold = 50;

    @Column(name = "scheduled_start")
    private Instant scheduledStart;

    @Column(name = "scheduled_end")
    private Instant scheduledEnd;

    @Column(name = "is_timed")
    private Boolean isTimed = true;

    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;

    @Column(name = "allow_review")
    private Boolean allowReview = true;

    @Column(name = "shuffle_questions")
    private Boolean shuffleQuestions = true;

    @Column(name = "shuffle_answers")
    private Boolean shuffleAnswers = true;

    @Column(name = "show_score_immediately")
    private Boolean showScoreImmediately = true;

    @Column(name = "max_attempts")
    private Integer maxAttempts = 1;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "published_at")
    private Instant publishedAt;

    public Exam() {}

    public Exam(Long classroomId, String title, ExamType examType, Long createdBy) {
        this.classroomId = classroomId;
        this.title = title;
        this.examType = examType;
        this.createdBy = createdBy;
        this.status = ExamStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    // Getters et Setters
    public Long getId() { return id; }

    public Long getClassroomId() { return classroomId; }
    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ExamType getExamType() { return examType; }
    public void setExamType(ExamType examType) { this.examType = examType; }

    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getPassThreshold() { return passThreshold; }
    public void setPassThreshold(Integer passThreshold) { this.passThreshold = passThreshold; }

    public Instant getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(Instant scheduledStart) { this.scheduledStart = scheduledStart; }

    public Instant getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(Instant scheduledEnd) { this.scheduledEnd = scheduledEnd; }

    public Boolean getIsTimed() { return isTimed; }
    public void setIsTimed(Boolean isTimed) { this.isTimed = isTimed; }

    public Boolean getIsScheduled() { return isScheduled; }
    public void setIsScheduled(Boolean isScheduled) { this.isScheduled = isScheduled; }

    public Boolean getAllowReview() { return allowReview; }
    public void setAllowReview(Boolean allowReview) { this.allowReview = allowReview; }

    public Boolean getShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(Boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }

    public Boolean getShuffleAnswers() { return shuffleAnswers; }
    public void setShuffleAnswers(Boolean shuffleAnswers) { this.shuffleAnswers = shuffleAnswers; }

    public Boolean getShowScoreImmediately() { return showScoreImmediately; }
    public void setShowScoreImmediately(Boolean showScoreImmediately) { this.showScoreImmediately = showScoreImmediately; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    // Méthodes utilitaires
    public boolean isAvailable() {
        if (status != ExamStatus.PUBLISHED && status != ExamStatus.SCHEDULED) {
            return false;
        }
        if (!isScheduled) {
            return true;
        }
        Instant now = Instant.now();
        return (scheduledStart == null || now.isAfter(scheduledStart)) &&
               (scheduledEnd == null || now.isBefore(scheduledEnd));
    }

    public boolean isExpired() {
        return scheduledEnd != null && Instant.now().isAfter(scheduledEnd);
    }
}

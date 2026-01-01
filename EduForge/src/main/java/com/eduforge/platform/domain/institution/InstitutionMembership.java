package com.eduforge.platform.domain.institution;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "institution_membership",
        uniqueConstraints = @UniqueConstraint(name = "uq_inst_user", columnNames = {"institution_id", "user_id"}))
public class InstitutionMembership {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "institution_id", nullable = false)
    private Long institutionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MembershipStatus status = MembershipStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public InstitutionMembership() {}

    public InstitutionMembership(Long institutionId, Long userId, MembershipStatus status) {
        this.institutionId = institutionId;
        this.userId = userId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

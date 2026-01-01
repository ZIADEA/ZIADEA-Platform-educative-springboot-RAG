package com.eduforge.platform.domain.auth;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_inst", columnList = "institution_id"),
        @Index(name = "idx_user_role", columnList = "role")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String fullName;

    @Email
    @NotBlank
    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status = AccountStatus.ACTIVE;

    // Affiliation à une institution (nullable = prof/étudiant libre)
    @Column(name = "institution_id")
    private Long institutionId;

    @Column(name = "program_id")
    private Long programId;

    @Column(name = "level_id")
    private Long levelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "affiliation_status", length = 30)
    private AffiliationStatus affiliationStatus = AffiliationStatus.INDEPENDENT;

    @Transient
    private String institutionName;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "avatar_path", length = 500)
    private String avatarPath;

    @Column(length = 30)
    private String phone;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public User() {}

    public User(String fullName, String email, String passwordHash, Role role, AccountStatus status) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.affiliationStatus = AffiliationStatus.INDEPENDENT;
        this.createdAt = Instant.now();
    }

    // Constructeur complet avec affiliation
    public User(String fullName, String email, String passwordHash, Role role, 
                AccountStatus status, Long institutionId, AffiliationStatus affiliationStatus) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
        this.institutionId = institutionId;
        this.affiliationStatus = affiliationStatus;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }

    public AffiliationStatus getAffiliationStatus() { return affiliationStatus; }
    public void setAffiliationStatus(AffiliationStatus affiliationStatus) { this.affiliationStatus = affiliationStatus; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Instant getLastLogin() { return lastLogin; }
    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Méthodes utilitaires
    public boolean isIndependent() {
        return institutionId == null || affiliationStatus == AffiliationStatus.INDEPENDENT;
    }

    public boolean isAffiliated() {
        return institutionId != null && affiliationStatus == AffiliationStatus.AFFILIATED;
    }

    public boolean isPendingAffiliation() {
        return affiliationStatus == AffiliationStatus.PENDING;
    }
}

package com.eduforge.platform.web.dto;

import com.eduforge.platform.domain.institution.InstitutionMembership;
import com.eduforge.platform.domain.institution.MembershipStatus;

import java.time.Instant;

/**
 * DTO enrichi pour afficher les demandes d'affiliation avec les infos utilisateur
 */
public class MembershipRequestDTO {
    private Long membershipId;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userRole;
    private MembershipStatus status;
    private Instant createdAt;

    public MembershipRequestDTO() {}

    public MembershipRequestDTO(InstitutionMembership membership, String userFullName, 
                                 String userEmail, String userRole) {
        this.membershipId = membership.getId();
        this.userId = membership.getUserId();
        this.status = membership.getStatus();
        this.createdAt = membership.getCreatedAt();
        this.userFullName = userFullName;
        this.userEmail = userEmail;
        this.userRole = userRole;
    }

    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public MembershipStatus getStatus() { return status; }
    public void setStatus(MembershipStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

package com.eduforge.platform.domain.institution;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "institution")
public class Institution {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un institution-manager "possède" une institution
    @Column(name = "owner_user_id", nullable = false, unique = true)
    private Long ownerUserId;

    @NotBlank
    @Column(nullable = false, length = 180)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InstitutionType type = InstitutionType.UNIVERSITE;

    @Column(nullable = false, length = 80)
    private String country = "Maroc";

    @Column(nullable = false, length = 80)
    private String city = "Meknès";

    @Column(nullable = true, length = 180)
    private String address;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "logo_path", length = 500)
    private String logoPath;

    @Column(length = 255)
    private String website;

    @Column(length = 30)
    private String phone;

    // Paramètres de notation
    @Enumerated(EnumType.STRING)
    @Column(name = "grading_scale", length = 20)
    private GradingScale gradingScale = GradingScale.SCALE_20;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_method", length = 20)
    private GradingMethod gradingMethod = GradingMethod.STANDARD;

    @Column(name = "default_pass_threshold")
    private Integer defaultPassThreshold = 50;

    @Column(name = "negative_marking")
    private Boolean negativeMarking = false;

    @Column(name = "correct_points", precision = 4, scale = 2)
    private BigDecimal correctPoints = new BigDecimal("1.00");

    @Column(name = "wrong_points", precision = 4, scale = 2)
    private BigDecimal wrongPoints = new BigDecimal("0.00");

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Institution() {}

    public Institution(Long ownerUserId, String name, InstitutionType type, String country, String city, String address) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.type = type;
        this.country = country;
        this.city = city;
        this.address = address;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public InstitutionType getType() { return type; }
    public void setType(InstitutionType type) { this.type = type; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public GradingScale getGradingScale() { return gradingScale; }
    public void setGradingScale(GradingScale gradingScale) { this.gradingScale = gradingScale; }

    public GradingMethod getGradingMethod() { return gradingMethod; }
    public void setGradingMethod(GradingMethod gradingMethod) { this.gradingMethod = gradingMethod; }

    public Integer getDefaultPassThreshold() { return defaultPassThreshold; }
    public void setDefaultPassThreshold(Integer defaultPassThreshold) { this.defaultPassThreshold = defaultPassThreshold; }

    public Boolean getNegativeMarking() { return negativeMarking; }
    public void setNegativeMarking(Boolean negativeMarking) { this.negativeMarking = negativeMarking; }

    public BigDecimal getCorrectPoints() { return correctPoints; }
    public void setCorrectPoints(BigDecimal correctPoints) { this.correctPoints = correctPoints; }

    public BigDecimal getWrongPoints() { return wrongPoints; }
    public void setWrongPoints(BigDecimal wrongPoints) { this.wrongPoints = wrongPoints; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

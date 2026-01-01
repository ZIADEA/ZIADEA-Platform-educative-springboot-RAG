package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Le nom complet est requis.")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "L'email est requis.")
    @Email(message = "Email invalide.")
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    @Size(min = 6, max = 72, message = "Le mot de passe doit contenir entre 6 et 72 caractères.")
    private String password;

    @NotBlank(message = "Le type de compte est requis.")
    private String accountType; // ETUDIANT, PROF, INSTITUTION

    // Champs pour l'affiliation à une institution
    private Long institutionId; // null = indépendant
    private boolean wantsAffiliation; // pour le checkbox du formulaire
    
    private String phone;

    // Pour les institutions uniquement
    private String institutionName;
    private String institutionType; // UNIVERSITE, LYCEE, COLLEGE, etc.
    private String country;
    private String city;
    private String address;
    
    // Getters et Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public boolean isWantsAffiliation() { return wantsAffiliation; }
    public void setWantsAffiliation(boolean wantsAffiliation) { this.wantsAffiliation = wantsAffiliation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstitutionType() { return institutionType; }
    public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Méthode utilitaire
    public boolean wantsInstitutionAffiliation() {
        return institutionId != null && institutionId > 0;
    }
}

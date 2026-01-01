package com.eduforge.platform.web.dto.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstitutionProfileForm {

    @NotBlank @Size(max = 180)
    private String name;

    @NotBlank
    private String type; // ECOLE / UNIVERSITE

    @NotBlank @Size(max = 80)
    private String country;

    @NotBlank @Size(max = 80)
    private String city;

    @Size(max = 180)
    private String address;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

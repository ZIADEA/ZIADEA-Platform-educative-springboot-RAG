package com.eduforge.platform.web.api;

import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.repository.InstitutionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final InstitutionRepository institutions;

    public ApiController(InstitutionRepository institutions) {
        this.institutions = institutions;
    }

    @GetMapping("/institutions/search")
    public List<InstitutionDto> searchInstitutions(@RequestParam("q") String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        String searchTerm = "%" + query.toLowerCase() + "%";
        
        return institutions.findAll().stream()
                .filter(i -> i.getName().toLowerCase().contains(query.toLowerCase()) ||
                        (i.getCity() != null && i.getCity().toLowerCase().contains(query.toLowerCase())))
                .limit(10)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private InstitutionDto toDto(Institution inst) {
        return new InstitutionDto(
                inst.getId(),
                inst.getName(),
                inst.getType() != null ? inst.getType().name() : null,
                inst.getCity(),
                inst.getCountry()
        );
    }

    public record InstitutionDto(Long id, String name, String type, String city, String country) {}
}

package com.eduforge.platform.service.institution;

import com.eduforge.platform.domain.institution.Institution;
import com.eduforge.platform.domain.institution.InstitutionType;
import com.eduforge.platform.repository.InstitutionRepository;
import com.eduforge.platform.web.dto.forms.InstitutionProfileForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstitutionService {

    private final InstitutionRepository institutions;

    public InstitutionService(InstitutionRepository institutions) {
        this.institutions = institutions;
    }

    public Institution getOrCreateByOwner(Long ownerUserId) {
        return institutions.findByOwnerUserId(ownerUserId)
                .orElseGet(() -> institutions.save(
                        new Institution(ownerUserId, "Mon Institution", InstitutionType.UNIVERSITE, "Maroc", "Meknès", null)
                ));
    }

    @Transactional
    public Institution updateProfile(Long ownerUserId, InstitutionProfileForm form) {
        Institution inst = getOrCreateByOwner(ownerUserId);
        inst.setName(form.getName().trim());
        inst.setType(InstitutionType.valueOf(form.getType()));
        inst.setCountry(form.getCountry().trim());
        inst.setCity(form.getCity().trim());
        inst.setAddress(form.getAddress() == null ? null : form.getAddress().trim());
        return inst;
    }
}

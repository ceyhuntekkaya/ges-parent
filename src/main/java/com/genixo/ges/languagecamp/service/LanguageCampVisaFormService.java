package com.genixo.ges.languagecamp.service;

import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LanguageCampVisaFormService {

    private final LanguageCampVisaFormRepository visaForms;

    public LanguageCampVisaFormService(LanguageCampVisaFormRepository visaForms) {
        this.visaForms = visaForms;
    }

    @Transactional
    public LanguageCampVisaForm createForApplication(LanguageCampApplication application) {
        LanguageCampVisaForm form = new LanguageCampVisaForm();
        form.setApplication(application);
        return visaForms.save(form);
    }

    @Transactional
    public LanguageCampVisaForm ensureForApplication(LanguageCampApplication application) {
        return visaForms.findByApplication_Id(application.getId())
            .orElseGet(() -> createForApplication(application));
    }
}

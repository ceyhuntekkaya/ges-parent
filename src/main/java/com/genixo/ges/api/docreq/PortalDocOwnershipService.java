package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PortalDocOwnershipService {

    private final UniversityApplicationRepository universityApps;
    private final LanguageCampApplicationRepository languageCampApps;

    public PortalDocOwnershipService(
        UniversityApplicationRepository universityApps,
        LanguageCampApplicationRepository languageCampApps
    ) {
        this.universityApps = universityApps;
        this.languageCampApps = languageCampApps;
    }

    public void assertOwner(DocumentRequirementScope scope, UUID applicationId, UUID currentUserId) {
        boolean ok = switch (scope) {
            case UNIVERSITY_APPLICATION, UNIVERSITY_REFERENCE ->
                universityApps.findByIdAndApplicant_Id(applicationId, currentUserId).isPresent();
            case LANGUAGE_CAMP_APPLICATION, LANGUAGE_CAMP_PARTICIPANT ->
                languageCampApps.findByIdAndApplicant_Id(applicationId, currentUserId).isPresent();
        };

        if (!ok) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }
}


package com.genixo.ges.api.university;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.ApplicationDocumentRepository;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.repo.StoredFileRepository;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import com.genixo.ges.university.model.UniversityApplicationPortfolioFile;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PortalUniversityApplicationFileService {

    private final UniversityApplicationRepository applications;
    private final ApplicationDocumentRepository applicationDocuments;
    private final StoredFileRepository storedFiles;

    public PortalUniversityApplicationFileService(
        UniversityApplicationRepository applications,
        ApplicationDocumentRepository applicationDocuments,
        StoredFileRepository storedFiles
    ) {
        this.applications = applications;
        this.applicationDocuments = applicationDocuments;
        this.storedFiles = storedFiles;
    }

    public StoredFile resolveForApplicant(UUID applicationId, UUID storedFileId, UUID applicantUserId) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(applicationId, applicantUserId)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden"));

        if (!isReferencedByApplication(ua, applicationId, storedFileId)) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return storedFiles.findById(storedFileId)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "File not found"));
    }

    private boolean isReferencedByApplication(UniversityApplication ua, UUID applicationId, UUID storedFileId) {
        String id = storedFileId.toString();

        if (ua.getDocuments() != null) {
            for (UniversityApplicationDocument doc : ua.getDocuments()) {
                if (urlContainsFileId(doc.getDocumentUrl(), id)) {
                    return true;
                }
            }
        }

        if (ua.getPortfolioSections() != null) {
            for (var section : ua.getPortfolioSections()) {
                if (section.getFiles() == null) {
                    continue;
                }
                for (UniversityApplicationPortfolioFile file : section.getFiles()) {
                    if (urlContainsFileId(file.getFileUrl(), id)) {
                        return true;
                    }
                }
            }
        }

        var pageable = PageRequest.of(0, 500);
        for (ApplicationDocument doc : applicationDocuments
            .findByScopeAndApplicationId(DocumentRequirementScope.UNIVERSITY_APPLICATION, applicationId, pageable)
            .getContent()) {
            if (doc.getFile() != null && storedFileId.equals(doc.getFile().getId())) {
                return true;
            }
        }

        return false;
    }

    private static boolean urlContainsFileId(String url, String fileId) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return url.contains(fileId);
    }
}

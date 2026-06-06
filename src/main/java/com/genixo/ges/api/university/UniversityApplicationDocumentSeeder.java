package com.genixo.ges.api.university;

import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class UniversityApplicationDocumentSeeder {

    private final DocumentRequirementRepository requirements;

    public UniversityApplicationDocumentSeeder(DocumentRequirementRepository requirements) {
        this.requirements = requirements;
    }

    public void seedFromActiveRequirements(UniversityApplication application) {
        List<DocumentRequirement> activeRequirements = requirements
            .findByScopeAndActiveTrue(DocumentRequirementScope.UNIVERSITY_APPLICATION, Pageable.unpaged())
            .getContent();

        if (activeRequirements.isEmpty()) {
            return;
        }

        List<UniversityApplicationDocument> documents = new ArrayList<>(activeRequirements.size());
        for (DocumentRequirement req : activeRequirements) {
            UniversityApplicationDocument doc = new UniversityApplicationDocument();
            doc.setApplication(application);
            doc.setRequired(req.isRequired());
            doc.setDocumentName(resolveDocumentName(req));
            doc.setDocumentDescription(req.getDescription());
            doc.setDocumentUrl(null);
            doc.setUploadedAt(null);
            documents.add(doc);
        }

        application.setDocuments(documents);
    }

    private static String resolveDocumentName(DocumentRequirement req) {
        String title = req.getTitle();
        if (title != null) {
            String trimmed = title.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return req.getKey();
    }
}

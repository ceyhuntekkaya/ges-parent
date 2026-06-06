package com.genixo.ges.api.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UniversityApplicationDocumentSeederTest {

    @Mock
    DocumentRequirementRepository requirements;

    @InjectMocks
    UniversityApplicationDocumentSeeder seeder;

    @Test
    void seedFromActiveRequirements_copiesActiveUniversityApplicationRequirements() {
        DocumentRequirement transcript = requirement("TRANSCRIPT", "Transcript", "Latest transcript.", true);
        DocumentRequirement cv = requirement("CV", null, "Academic CV.", false);

        when(requirements.findByScopeAndActiveTrue(eq(DocumentRequirementScope.UNIVERSITY_APPLICATION), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(transcript, cv)));

        UniversityApplication application = new UniversityApplication();
        seeder.seedFromActiveRequirements(application);

        List<UniversityApplicationDocument> documents = application.getDocuments();
        assertThat(documents).hasSize(2);

        assertThat(documents.get(0).getApplication()).isSameAs(application);
        assertThat(documents.get(0).getDocumentName()).isEqualTo("Transcript");
        assertThat(documents.get(0).getDocumentDescription()).isEqualTo("Latest transcript.");
        assertThat(documents.get(0).getRequired()).isTrue();
        assertThat(documents.get(0).getDocumentUrl()).isNull();
        assertThat(documents.get(0).getUploadedAt()).isNull();

        assertThat(documents.get(1).getDocumentName()).isEqualTo("CV");
        assertThat(documents.get(1).getRequired()).isFalse();
    }

    @Test
    void seedFromActiveRequirements_usesKeyWhenTitleMissing() {
        DocumentRequirement req = requirement("DIPLOMA", "  ", null, false);
        when(requirements.findByScopeAndActiveTrue(eq(DocumentRequirementScope.UNIVERSITY_APPLICATION), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(req)));

        UniversityApplication application = new UniversityApplication();
        seeder.seedFromActiveRequirements(application);

        assertThat(application.getDocuments()).singleElement()
            .extracting(UniversityApplicationDocument::getDocumentName)
            .isEqualTo("DIPLOMA");
    }

    private static DocumentRequirement requirement(String key, String title, String description, boolean required) {
        DocumentRequirement req = new DocumentRequirement();
        req.setScope(DocumentRequirementScope.UNIVERSITY_APPLICATION);
        req.setKey(key);
        req.setTitle(title);
        req.setDescription(description);
        req.setRequired(required);
        req.setActive(true);
        return req;
    }
}

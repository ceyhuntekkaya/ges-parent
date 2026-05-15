package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampVisaFormDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageCampVisaFormDocumentRepository extends JpaRepository<LanguageCampVisaFormDocument, UUID> {
    Optional<LanguageCampVisaFormDocument> findByIdAndVisaForm_Id(UUID id, UUID visaFormId);
}

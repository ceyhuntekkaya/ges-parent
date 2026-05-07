package com.genixo.ges.docreq.repo;

import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, UUID> {
    Page<ApplicationDocument> findByScopeAndApplicationId(DocumentRequirementScope scope, UUID applicationId, Pageable pageable);
    Optional<ApplicationDocument> findByIdAndUploadedBy_Id(UUID id, UUID uploadedByUserId);
}


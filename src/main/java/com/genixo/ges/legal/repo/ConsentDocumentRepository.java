package com.genixo.ges.legal.repo;

import com.genixo.ges.legal.model.ConsentDocument;
import com.genixo.ges.legal.model.ConsentType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentDocumentRepository extends JpaRepository<ConsentDocument, UUID> {
    Page<ConsentDocument> findByActiveTrue(Pageable pageable);
    Page<ConsentDocument> findByTypeAndActiveTrue(ConsentType type, Pageable pageable);
    Optional<ConsentDocument> findByTypeAndLanguageAndVersion(ConsentType type, String language, String version);
    Optional<ConsentDocument> findFirstByTypeAndLanguageAndActiveTrueOrderByCreatedAtDesc(ConsentType type, String language);
}


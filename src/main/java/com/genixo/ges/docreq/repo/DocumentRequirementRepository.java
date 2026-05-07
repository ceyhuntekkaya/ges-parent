package com.genixo.ges.docreq.repo;

import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRequirementRepository extends JpaRepository<DocumentRequirement, UUID> {
    Optional<DocumentRequirement> findByScopeAndKey(DocumentRequirementScope scope, String key);
    Page<DocumentRequirement> findByScopeAndActiveTrue(DocumentRequirementScope scope, Pageable pageable);
}


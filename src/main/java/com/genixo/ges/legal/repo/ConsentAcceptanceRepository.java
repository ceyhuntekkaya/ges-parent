package com.genixo.ges.legal.repo;

import com.genixo.ges.legal.model.ConsentAcceptance;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentAcceptanceRepository extends JpaRepository<ConsentAcceptance, UUID> {
    Optional<ConsentAcceptance> findFirstByUser_IdAndDocument_IdOrderByAcceptedAtDesc(UUID userId, UUID documentId);
}


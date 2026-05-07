package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageCampApplicationRepository extends JpaRepository<LanguageCampApplication, UUID> {
    Page<LanguageCampApplication> findByApplicant_Id(UUID applicantUserId, Pageable pageable);
    Optional<LanguageCampApplication> findByIdAndApplicant_Id(UUID id, UUID applicantUserId);
}


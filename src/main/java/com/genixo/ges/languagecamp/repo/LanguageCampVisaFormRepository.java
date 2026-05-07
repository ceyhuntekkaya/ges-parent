package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageCampVisaFormRepository extends JpaRepository<LanguageCampVisaForm, UUID> {
    Optional<LanguageCampVisaForm> findByParticipant_Id(UUID participantId);
    Page<LanguageCampVisaForm> findAllByParticipant_Application_Id(UUID applicationId, Pageable pageable);
}


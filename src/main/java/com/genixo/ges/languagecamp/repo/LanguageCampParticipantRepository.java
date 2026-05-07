package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampParticipant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageCampParticipantRepository extends JpaRepository<LanguageCampParticipant, UUID> {
    Optional<LanguageCampParticipant> findByIdAndApplication_Applicant_Id(UUID id, UUID applicantUserId);
}


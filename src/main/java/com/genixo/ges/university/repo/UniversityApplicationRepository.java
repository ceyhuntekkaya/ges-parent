package com.genixo.ges.university.repo;

import com.genixo.ges.university.model.UniversityApplication;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityApplicationRepository extends JpaRepository<UniversityApplication, UUID> {
    Page<UniversityApplication> findByApplicant_Id(UUID applicantUserId, Pageable pageable);
    Optional<UniversityApplication> findByIdAndApplicant_Id(UUID id, UUID applicantUserId);
}


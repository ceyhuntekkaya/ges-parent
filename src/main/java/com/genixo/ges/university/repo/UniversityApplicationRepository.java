package com.genixo.ges.university.repo;

import com.genixo.ges.university.model.UniversityApplication;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface UniversityApplicationRepository extends JpaRepository<UniversityApplication, UUID> {
    @Override
    @EntityGraph(attributePaths = {"applicantProfile"})
    Page<UniversityApplication> findAll(Pageable pageable);

    Page<UniversityApplication> findByApplicant_Id(UUID applicantUserId, Pageable pageable);
    Optional<UniversityApplication> findByIdAndApplicant_Id(UUID id, UUID applicantUserId);
}


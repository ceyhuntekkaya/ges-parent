package com.genixo.ges.applicant.repo;

import com.genixo.ges.applicant.model.ApplicantProfile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile, UUID> {
    Optional<ApplicantProfile> findByUser_Id(UUID userId);
    boolean existsByUser_EmailIgnoreCase(String email);
}


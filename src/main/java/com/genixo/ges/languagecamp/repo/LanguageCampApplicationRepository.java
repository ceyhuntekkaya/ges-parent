package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LanguageCampApplicationRepository extends JpaRepository<LanguageCampApplication, UUID> {
    Page<LanguageCampApplication> findByApplicant_Id(UUID applicantUserId, Pageable pageable);

    List<LanguageCampApplication> findByApplicant_IdOrderByCreatedAtDesc(UUID applicantUserId);

    List<LanguageCampApplication> findByApplicant_IdAndLanguageCampProject_IdOrderByCreatedAtAsc(
        UUID applicantUserId,
        UUID languageCampProjectId
    );

    Optional<LanguageCampApplication> findByIdAndApplicant_Id(UUID id, UUID applicantUserId);

    @Query("""
        SELECT DISTINCT a FROM LanguageCampApplication a
        LEFT JOIN FETCH a.visaForm vf
        LEFT JOIN FETCH vf.documents doc
        LEFT JOIN FETCH doc.storedFile
        WHERE a.id = :id AND a.applicant.id = :applicantUserId
        """)
    Optional<LanguageCampApplication> findDetailByIdAndApplicant_Id(
        @Param("id") UUID id,
        @Param("applicantUserId") UUID applicantUserId
    );

    @Query("""
        SELECT DISTINCT a FROM LanguageCampApplication a
        LEFT JOIN FETCH a.visaForm vf
        LEFT JOIN FETCH vf.documents doc
        LEFT JOIN FETCH doc.storedFile
        WHERE a.id = :id
        """)
    Optional<LanguageCampApplication> findDetailById(@Param("id") UUID id);
}


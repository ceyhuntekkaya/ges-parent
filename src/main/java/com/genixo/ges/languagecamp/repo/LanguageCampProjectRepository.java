package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.model.EProjectStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LanguageCampProjectRepository extends JpaRepository<LanguageCampProject, UUID> {
    Page<LanguageCampProject> findByProjectStatusAndIndividual(
        EProjectStatus projectStatus,
        boolean individual,
        Pageable pageable
    );

    @Query("""
        SELECT p FROM LanguageCampProject p
        LEFT JOIN p.company c
        WHERE p.projectStatus = :status
        AND p.individual = :individual
        AND p.applicationStartAt IS NOT NULL
        AND p.applicationEndAt IS NOT NULL
        AND :now >= p.applicationStartAt
        AND :now <= p.applicationEndAt
        AND p.quota IS NOT NULL
        AND (
            SELECT COUNT(a) FROM LanguageCampApplication a
            WHERE a.languageCampProject = p
        ) < p.quota
        AND (:companyCode IS NULL OR c.code = :companyCode)
        """)
    Page<LanguageCampProject> findOpenForApplication(
        @Param("status") EProjectStatus status,
        @Param("individual") boolean individual,
        @Param("now") Instant now,
        @Param("companyCode") String companyCode,
        Pageable pageable
    );

    @Query("""
        SELECT p FROM LanguageCampProject p
        INNER JOIN p.company c
        WHERE p.projectStatus = :status
        AND COALESCE(p.individual, false) = false
        AND c.code = :companyCode
        """)
    Page<LanguageCampProject> findActiveCorporateByCompanyCode(
        @Param("status") EProjectStatus status,
        @Param("companyCode") String companyCode,
        Pageable pageable
    );

    java.util.Optional<LanguageCampProject> findByIdAndProjectStatus(UUID id, EProjectStatus projectStatus);
}


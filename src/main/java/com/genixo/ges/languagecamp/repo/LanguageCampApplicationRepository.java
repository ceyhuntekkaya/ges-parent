package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import java.util.Collection;
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
        SELECT a FROM LanguageCampApplication a
        JOIN FETCH a.applicant applicant
        LEFT JOIN FETCH applicant.applicantProfile
        JOIN FETCH a.languageCampProject
        WHERE a.id = :id
        """)
    Optional<LanguageCampApplication> findByIdWithApplicant(@Param("id") UUID id);

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

    @Query("SELECT DISTINCT a FROM LanguageCampApplication a JOIN FETCH a.tasks t WHERE t.status = :taskStatus")
    List<LanguageCampApplication> findAllWithTasksByTaskStatus(
        @Param("taskStatus") com.genixo.ges.university.model.UniversityApplicationTaskStatus taskStatus
    );

    long countByLanguageCampProject_Id(UUID languageCampProjectId);

    @Query("""
        SELECT a.languageCampProject.id, COUNT(a)
        FROM LanguageCampApplication a
        WHERE a.languageCampProject.id IN :projectIds
        GROUP BY a.languageCampProject.id
        """)
    List<Object[]> countGroupedByProjectId(@Param("projectIds") Collection<UUID> projectIds);

    @Query("""
        SELECT a FROM LanguageCampApplication a
        WHERE (:projectId IS NULL OR a.languageCampProject.id = :projectId)
        AND (:status IS NULL OR a.status = :status)
        AND (:paymentCompleted IS NULL OR a.paymentCompleted = :paymentCompleted)
        """)
    Page<LanguageCampApplication> findAdminList(
        @Param("projectId") UUID projectId,
        @Param("status") ApplicationStatus status,
        @Param("paymentCompleted") Boolean paymentCompleted,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT a.applicant_user_id AS "applicantUserId",
                   a.language_camp_project_id AS "languageCampProjectId",
                   MIN(a.created_at) AS "createdAt",
                   MAX(a.updated_at) AS "updatedAt",
                   COUNT(*) AS "participantCount"
            FROM language_camp_applications a
            WHERE (CAST(:projectId AS uuid) IS NULL OR a.language_camp_project_id = CAST(:projectId AS uuid))
              AND (CAST(:status AS varchar) IS NULL OR EXISTS (
                  SELECT 1 FROM language_camp_applications f
                  WHERE f.applicant_user_id = a.applicant_user_id
                    AND f.language_camp_project_id = a.language_camp_project_id
                    AND f.status = CAST(:status AS varchar)
              ))
              AND (CAST(:paymentCompleted AS boolean) IS NULL OR EXISTS (
                  SELECT 1 FROM language_camp_applications f
                  WHERE f.applicant_user_id = a.applicant_user_id
                    AND f.language_camp_project_id = a.language_camp_project_id
                    AND f.payment_completed = CAST(:paymentCompleted AS boolean)
              ))
            GROUP BY a.applicant_user_id, a.language_camp_project_id
            ORDER BY MAX(a.updated_at) DESC
            """,
        countQuery = """
            SELECT COUNT(*) FROM (
                SELECT 1
                FROM language_camp_applications a
                WHERE (CAST(:projectId AS uuid) IS NULL OR a.language_camp_project_id = CAST(:projectId AS uuid))
                  AND (CAST(:status AS varchar) IS NULL OR EXISTS (
                      SELECT 1 FROM language_camp_applications f
                      WHERE f.applicant_user_id = a.applicant_user_id
                        AND f.language_camp_project_id = a.language_camp_project_id
                        AND f.status = CAST(:status AS varchar)
                  ))
                  AND (CAST(:paymentCompleted AS boolean) IS NULL OR EXISTS (
                      SELECT 1 FROM language_camp_applications f
                      WHERE f.applicant_user_id = a.applicant_user_id
                        AND f.language_camp_project_id = a.language_camp_project_id
                        AND f.payment_completed = CAST(:paymentCompleted AS boolean)
                  ))
                GROUP BY a.applicant_user_id, a.language_camp_project_id
            ) grouped
            """,
        nativeQuery = true
    )
    Page<LanguageCampApplicationGroupKeyRow> findAdminGroupKeys(
        @Param("projectId") UUID projectId,
        @Param("status") String status,
        @Param("paymentCompleted") Boolean paymentCompleted,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM LanguageCampApplication a
        JOIN FETCH a.applicant applicant
        LEFT JOIN FETCH applicant.applicantProfile
        JOIN FETCH a.languageCampProject
        WHERE a.applicant.id = :applicantUserId
          AND a.languageCampProject.id = :projectId
        ORDER BY a.createdAt ASC
        """)
    List<LanguageCampApplication> findAdminGroupApplications(
        @Param("applicantUserId") UUID applicantUserId,
        @Param("projectId") UUID projectId
    );
}


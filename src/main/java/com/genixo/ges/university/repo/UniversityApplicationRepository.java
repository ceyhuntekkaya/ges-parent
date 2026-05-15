package com.genixo.ges.university.repo;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UniversityApplicationRepository extends JpaRepository<UniversityApplication, UUID> {
    @Override
    @EntityGraph(attributePaths = {"applicantProfile"})
    Page<UniversityApplication> findAll(Pageable pageable);

    Page<UniversityApplication> findByApplicant_Id(UUID applicantUserId, Pageable pageable);
    Optional<UniversityApplication> findByIdAndApplicant_Id(UUID id, UUID applicantUserId);

    @Query("SELECT DISTINCT ua FROM UniversityApplication ua JOIN FETCH ua.tasks t WHERE t.status = :taskStatus")
    List<UniversityApplication> findAllWithTasksByTaskStatus(@Param("taskStatus") UniversityApplicationTaskStatus taskStatus);

    @EntityGraph(attributePaths = {"applicantProfile"})
    List<UniversityApplication> findByStatus(ApplicationStatus status);
}


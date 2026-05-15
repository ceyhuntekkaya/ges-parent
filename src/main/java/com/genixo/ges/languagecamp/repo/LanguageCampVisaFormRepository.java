package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LanguageCampVisaFormRepository extends JpaRepository<LanguageCampVisaForm, UUID> {
    Optional<LanguageCampVisaForm> findByApplication_Id(UUID applicationId);

    Page<LanguageCampVisaForm> findAllByApplication_Id(UUID applicationId, Pageable pageable);

    @Query("""
        SELECT DISTINCT vf FROM LanguageCampVisaForm vf
        LEFT JOIN FETCH vf.documents doc
        LEFT JOIN FETCH doc.storedFile
        WHERE vf.id = :id
        """)
    Optional<LanguageCampVisaForm> findByIdWithDocuments(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT vf FROM LanguageCampVisaForm vf
        LEFT JOIN FETCH vf.documents doc
        LEFT JOIN FETCH doc.storedFile
        WHERE vf.application.id = :applicationId
        """)
    Optional<LanguageCampVisaForm> findByApplicationIdWithDocuments(@Param("applicationId") UUID applicationId);
}

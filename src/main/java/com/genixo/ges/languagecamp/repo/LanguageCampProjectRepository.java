package com.genixo.ges.languagecamp.repo;

import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.model.EProjectStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageCampProjectRepository extends JpaRepository<LanguageCampProject, UUID> {
    Page<LanguageCampProject> findByProjectStatusAndIndividual(
        EProjectStatus projectStatus,
        boolean individual,
        Pageable pageable
    );

    java.util.Optional<LanguageCampProject> findByIdAndProjectStatus(UUID id, EProjectStatus projectStatus);
}


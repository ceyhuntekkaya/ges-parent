package com.genixo.ges.catalog.repo;

import com.genixo.ges.catalog.model.University;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    Page<University> findByActiveTrue(Pageable pageable);
    Page<University> findByCountry_IdAndActiveTrue(UUID countryId, Pageable pageable);
    Page<University> findByNameContainingIgnoreCaseAndActiveTrue(String q, Pageable pageable);
}


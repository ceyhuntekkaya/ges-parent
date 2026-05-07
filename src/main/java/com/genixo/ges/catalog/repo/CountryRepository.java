package com.genixo.ges.catalog.repo;

import com.genixo.ges.catalog.model.Country;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, UUID> {
    Optional<Country> findByCodeIgnoreCase(String code);
    Page<Country> findByNameContainingIgnoreCase(String q, Pageable pageable);
}


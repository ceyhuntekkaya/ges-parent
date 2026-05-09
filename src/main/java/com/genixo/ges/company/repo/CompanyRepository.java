package com.genixo.ges.company.repo;

import com.genixo.ges.company.model.Company;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Page<Company> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Company> findByCode(String code);
}


package com.genixo.ges.university.repo;

import com.genixo.ges.university.model.PortfolioSection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioSectionRepository extends JpaRepository<PortfolioSection, UUID> {

    List<PortfolioSection> findByActiveTrueOrderBySortOrderAscIdAsc();

    Page<PortfolioSection> findByNameContainingIgnoreCase(String name, Pageable pageable);
}


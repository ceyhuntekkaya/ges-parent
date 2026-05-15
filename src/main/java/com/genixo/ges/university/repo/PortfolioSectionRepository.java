package com.genixo.ges.university.repo;

import com.genixo.ges.university.model.PortfolioSection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioSectionRepository extends JpaRepository<PortfolioSection, UUID> {}


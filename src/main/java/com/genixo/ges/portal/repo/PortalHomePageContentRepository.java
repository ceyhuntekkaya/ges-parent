package com.genixo.ges.portal.repo;

import com.genixo.ges.portal.model.PortalHomePageContent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortalHomePageContentRepository extends JpaRepository<PortalHomePageContent, UUID> {
}

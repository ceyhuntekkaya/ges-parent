package com.genixo.ges.api.university.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationPortfolioSectionDto {
    UUID id;
    Boolean required;
    Integer sortOrder;

    UUID portfolioSectionId;
    PortfolioSectionDto portfolioSection;

    String sectionNameOverride;
    String sectionDescriptionOverride;

    List<UniversityApplicationPortfolioFileDto> files;

    Instant createdAt;
    Instant updatedAt;
}


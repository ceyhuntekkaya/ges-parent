package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.PortfolioFileType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationPortfolioFileDto {
    UUID id;
    PortfolioFileType type;
    String name;
    String description;
    String fileUrl;
    Instant createdAt;
    Instant updatedAt;
}


package com.genixo.ges.api.university.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PortfolioSectionDto {
    UUID id;
    String name;
    String description;
    Instant createdAt;
    Instant updatedAt;
}


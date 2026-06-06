package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.EducationLevel;
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
    EducationLevel educationLevel;
    String departmentKeyword;
    Integer sortOrder;
    Boolean defaultRequired;
    Boolean active;
    Instant createdAt;
    Instant updatedAt;
}


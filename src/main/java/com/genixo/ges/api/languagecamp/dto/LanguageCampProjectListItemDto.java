package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.EProjectStatus;
import com.genixo.ges.languagecamp.model.EProjectType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampProjectListItemDto {
    UUID id;
    String title;
    UUID companyId;
    Boolean individual;
    EProjectStatus projectStatus;
    EProjectType projectType;
    Integer quota;
    Long applicationCount;
    Instant createdAt;
    Instant updatedAt;
}


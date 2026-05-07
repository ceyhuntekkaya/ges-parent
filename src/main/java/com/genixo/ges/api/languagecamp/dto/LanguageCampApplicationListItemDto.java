package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationListItemDto {
    UUID id;
    ApplicationStatus status;
    LanguageCampCategory category;
    Instant createdAt;
    Instant updatedAt;
}


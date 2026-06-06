package com.genixo.ges.api.languagecamp.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationDocumentDto {
    UUID id;
    Boolean required;
    String documentName;
    String documentDescription;
    String documentUrl;
    Instant uploadedAt;
    Instant createdAt;
    Instant updatedAt;
}

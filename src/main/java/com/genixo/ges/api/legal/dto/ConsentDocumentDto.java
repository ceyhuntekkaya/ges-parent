package com.genixo.ges.api.legal.dto;

import com.genixo.ges.legal.model.ConsentType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsentDocumentDto {
    UUID id;
    ConsentType type;
    String language;
    String version;
    boolean active;
    String text;
    Instant createdAt;
    Instant updatedAt;
}


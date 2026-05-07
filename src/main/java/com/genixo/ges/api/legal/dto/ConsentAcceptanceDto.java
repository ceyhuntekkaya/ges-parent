package com.genixo.ges.api.legal.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsentAcceptanceDto {
    UUID id;
    UUID userId;
    UUID consentDocumentId;
    Instant acceptedAt;
    String ipAddress;
    String userAgent;
    String module;
    UUID applicationId;
}


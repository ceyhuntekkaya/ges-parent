package com.genixo.ges.api.legal.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Value;

@Value
public class ConsentAcceptanceRequestDto {
    @NotNull
    UUID consentDocumentId;

    String module;
    UUID applicationId;
}


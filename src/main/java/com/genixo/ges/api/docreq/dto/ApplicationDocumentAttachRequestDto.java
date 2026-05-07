package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.docreq.model.DocumentRequirementScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Value;

@Value
public class ApplicationDocumentAttachRequestDto {
    @NotNull
    DocumentRequirementScope scope;

    @NotNull
    UUID applicationId;

    UUID relatedEntityId;

    UUID requirementId;

    @NotBlank
    String requirementKey;

    @NotNull
    UUID fileId;
}


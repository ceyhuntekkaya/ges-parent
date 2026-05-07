package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.docreq.model.DocumentRequirementScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentRequirementUpsertRequestDto {
    @NotNull
    DocumentRequirementScope scope;

    @Size(max = 32)
    String category;

    @NotBlank
    @Size(max = 128)
    String key;

    @NotNull
    Boolean required;

    @Size(max = 255)
    String allowedContentTypes;

    Long maxSizeBytes;

    @Size(max = 255)
    String title;

    String description;

    Boolean active;
}


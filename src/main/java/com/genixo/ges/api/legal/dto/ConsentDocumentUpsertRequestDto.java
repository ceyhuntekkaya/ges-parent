package com.genixo.ges.api.legal.dto;

import com.genixo.ges.legal.model.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class ConsentDocumentUpsertRequestDto {
    @NotNull
    ConsentType type;

    @NotBlank
    @Size(max = 16)
    String language; // tr/en

    @NotBlank
    @Size(max = 32)
    String version;

    @NotNull
    Boolean active;

    @NotBlank
    String text;
}


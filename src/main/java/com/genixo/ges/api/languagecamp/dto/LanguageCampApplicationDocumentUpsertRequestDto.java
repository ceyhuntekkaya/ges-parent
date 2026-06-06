package com.genixo.ges.api.languagecamp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageCampApplicationDocumentUpsertRequestDto {
    Boolean required;

    @NotBlank
    String documentName;

    String documentDescription;

    String documentUrl;
}

package com.genixo.ges.api.languagecamp.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageCampVisaFormDocumentAttachRequestDto {
    @NotNull
    UUID fileId;
}

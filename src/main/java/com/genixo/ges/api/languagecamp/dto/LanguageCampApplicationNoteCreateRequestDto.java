package com.genixo.ges.api.languagecamp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageCampApplicationNoteCreateRequestDto {
    @NotBlank
    String todoText;
}

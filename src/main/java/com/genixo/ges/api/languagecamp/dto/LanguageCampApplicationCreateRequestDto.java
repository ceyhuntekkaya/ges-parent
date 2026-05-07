package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class LanguageCampApplicationCreateRequestDto {
    @NotNull
    LanguageCampCategory category;
}


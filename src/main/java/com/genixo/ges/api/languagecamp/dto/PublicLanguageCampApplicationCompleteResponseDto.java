package com.genixo.ges.api.languagecamp.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PublicLanguageCampApplicationCompleteResponseDto {
    UUID applicationId;
}


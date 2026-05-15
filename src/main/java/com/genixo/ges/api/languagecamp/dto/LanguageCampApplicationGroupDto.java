package com.genixo.ges.api.languagecamp.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationGroupDto {
    UUID projectId;
    LanguageCampProjectDetailDto project;
    List<LanguageCampApplicationDetailDto> participants;
}

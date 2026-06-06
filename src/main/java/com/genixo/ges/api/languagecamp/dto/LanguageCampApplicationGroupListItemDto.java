package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationGroupListItemDto {
    UUID applicantUserId;
    String applicantEmail;
    String applicantDisplayName;
    UUID languageCampProjectId;
    String languageCampProjectTitle;
    LanguageCampCategory category;
    int participantCount;
    UUID primaryApplicationId;
    List<LanguageCampApplicationListItemDto> participants;
    Instant createdAt;
    Instant updatedAt;
}

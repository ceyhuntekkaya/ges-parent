package com.genixo.ges.api.languagecamp.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationMeetingDto {
    UUID id;
    String person;
    Instant meetingAt;
    String meetingNote;
    String meetingResult;
    Instant createdAt;
    Instant updatedAt;
}

package com.genixo.ges.api.university.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationMeetingDto {
    UUID id;
    String person;
    Instant meetingAt;
    String meetingNote;
    String meetingResult;
    Instant createdAt;
    Instant updatedAt;
}


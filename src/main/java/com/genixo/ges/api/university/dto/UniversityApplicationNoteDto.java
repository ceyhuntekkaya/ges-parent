package com.genixo.ges.api.university.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationNoteDto {
    UUID id;
    String writtenBy;
    Instant writtenAt;
    String todoText;
    Instant createdAt;
    Instant updatedAt;
}


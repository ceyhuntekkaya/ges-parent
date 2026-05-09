package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.EducationLevel;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationListItemDto {
    UUID id;
    String firstName;
    String lastName;
    ApplicationStatus status;
    EducationLevel educationLevel;
    Instant createdAt;
    Instant updatedAt;
}


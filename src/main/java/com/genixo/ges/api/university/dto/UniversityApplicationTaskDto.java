package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationTaskDto {
    UUID id;
    Instant scheduledAt;
    String withWhom;
    String whatToDo;
    UniversityApplicationTaskStatus status;
    String performedByUser;
    Instant createdAt;
    Instant updatedAt;
}


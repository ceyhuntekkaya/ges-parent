package com.genixo.ges.api.university.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PendingTaskListItemDto {

    // Başvuru sahibi
    UUID applicationId;
    String applicantFirstName;
    String applicantLastName;

    // Takip eden kişi
    String followerPerson;

    // Görev detayları
    UUID taskId;
    Instant scheduledAt;
    String withWhom;
    String whatToDo;
    Instant taskCreatedAt;
    Instant taskUpdatedAt;
}

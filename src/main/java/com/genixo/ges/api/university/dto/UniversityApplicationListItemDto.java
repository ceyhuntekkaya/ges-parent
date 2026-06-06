package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.EducationLevel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
    String followerPerson;
    BigDecimal priceAmount;
    String priceCurrency;
    BigDecimal totalPaidAmount;
    int pendingTaskCount;
    int completedTaskCount;
    List<Instant> pendingTaskScheduledAts;
    int meetingCount;
    int documentCount;
    int documentsWithFileCount;
    Instant createdAt;
    Instant updatedAt;
}


package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationListItemDto {
    UUID id;
    String firstName;
    String lastName;
    Boolean isItSelf;
    ApplicationStatus status;
    LanguageCampCategory category;
    UUID languageCampProjectId;
    String languageCampProjectTitle;
    boolean paymentCompleted;
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

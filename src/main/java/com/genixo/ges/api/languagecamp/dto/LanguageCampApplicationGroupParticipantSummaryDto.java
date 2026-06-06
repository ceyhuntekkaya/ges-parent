package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationGroupParticipantSummaryDto {
    UUID id;
    String firstName;
    String lastName;
    ApplicationStatus status;
    Boolean isItSelf;
    Integer participantIndex;
    boolean paymentCompleted;
    BigDecimal priceAmount;
    String priceCurrency;
    BigDecimal totalPaidAmount;
    Instant createdAt;
    Instant updatedAt;
}

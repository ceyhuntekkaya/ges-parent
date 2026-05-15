package com.genixo.ges.api.university.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationPaymentDto {
    UUID id;
    Instant paymentAt;
    BigDecimal amount;
    String currency;
    String receivedBy;
    Instant createdAt;
    Instant updatedAt;
}


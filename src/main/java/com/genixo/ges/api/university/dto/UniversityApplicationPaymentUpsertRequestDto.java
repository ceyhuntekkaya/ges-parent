package com.genixo.ges.api.university.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationPaymentUpsertRequestDto {
    @NotNull
    Instant paymentAt;

    @NotNull
    BigDecimal amount;

    @NotBlank
    String currency;

    String receivedBy;
}


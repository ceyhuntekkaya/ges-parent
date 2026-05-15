package com.genixo.ges.api.languagecamp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageCampPaymentCompletedRequestDto {
    @NotNull
    Boolean paymentCompleted;
}

package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.PassportType;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageCampVisaFormUpsertRequestDto {
    @Size(max = 64)
    String passportNumber;

    LocalDate passportValidUntil;

    PassportType passportType;

    LocalDate visaValidFrom;

    LocalDate visaValidUntil;

    @Size(max = 128)
    String visaIssuingCountry;

    @Size(max = 128)
    String visaType;
}

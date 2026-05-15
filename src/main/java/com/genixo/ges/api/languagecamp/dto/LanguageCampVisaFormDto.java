package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.PassportType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampVisaFormDto {
    UUID id;
    UUID applicationId;

    String passportNumber;
    LocalDate passportValidUntil;
    PassportType passportType;
    LocalDate visaValidFrom;
    LocalDate visaValidUntil;
    String visaIssuingCountry;
    String visaType;

    List<LanguageCampVisaFormDocumentDto> documents;

    Instant createdAt;
    Instant updatedAt;
}

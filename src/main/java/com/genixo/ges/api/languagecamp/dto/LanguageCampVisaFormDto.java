package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.common.jpa.Address;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampVisaFormDto {
    UUID id;
    UUID participantId;
    UUID applicationId;

    String birthPlace;
    String birthCountry;
    Address residenceAddress;
    Boolean visaRejectedBefore;
    String visaRejectionDetails;
    List<String> visitedCountries;

    UUID bankStatementFileId;
    UUID biometricPhotoFileId;
    String appointmentCityPreference;

    Instant createdAt;
    Instant updatedAt;
}


package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.common.jpa.Address;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampVisaFormUpsertRequestDto {
    UUID participantId;

    @Size(max = 128)
    String birthPlace;
    @Size(max = 128)
    String birthCountry;

    Address residenceAddress;

    Boolean visaRejectedBefore;
    String visaRejectionDetails;

    List<String> visitedCountries;

    UUID bankStatementFileId;
    UUID biometricPhotoFileId;

    @Size(max = 128)
    String appointmentCityPreference;
}


package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationUpdateRequestDto {
    LanguageCampCategory category;
    UUID programId;
    LocalDate startDate;
    LocalDate endDate;
    AccommodationType accommodationType;
    Boolean visaNeeded;
    Boolean visaFollowByGes;
    EmergencyContact emergencyContact;
    PaymentPreference paymentPreference;

    // corporate fields
    @Size(max = 255)
    String companyName;
    @Size(max = 64)
    String taxNumber;
    @Size(max = 128)
    String companyContactFullName;
    @Size(max = 32)
    String companyContactPhone;
    @Email
    @Size(max = 255)
    String companyContactEmail;
}


package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationUpdateRequestDto {
    LanguageCampCategory category;
    UUID programId;
    AccommodationType accommodationType;
    Boolean visaNeeded;
    Boolean visaFollowByGes;
    EmergencyContact emergencyContact;
    PaymentPreference paymentPreference;

    String companyCode;

    String firstName;
    String lastName;
    LocalDate birthDate;
    String phone;
    Boolean isItSelf;
    Integer numberOfApplicant;

    Boolean under18;
    String parentFullName;
    String parentPhoneNumber;
    String parentEmailAddress;
    String parentRelationship;
    String userNotes;
}


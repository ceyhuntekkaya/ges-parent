package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import java.time.LocalDate;
import lombok.Value;

@Value
public class LanguageCampParticipantCreateRequestDto {
    String firstName;
    String lastName;
    LocalDate birthDate;
    String phone;
    Boolean isItSelf;
    Boolean under18;
    String parentFullName;
    String parentPhoneNumber;
    String parentEmailAddress;
    String parentRelationship;
    String userNotes;
    AccommodationType accommodationType;
    Boolean visaNeeded;
    Boolean visaFollowByGes;
    PaymentPreference paymentPreference;
    EmergencyContact emergencyContact;
}

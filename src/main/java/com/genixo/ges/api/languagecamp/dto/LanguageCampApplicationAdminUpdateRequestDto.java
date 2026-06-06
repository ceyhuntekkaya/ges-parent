package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageCampApplicationAdminUpdateRequestDto {
    String firstName;
    String lastName;
    LocalDate birthDate;
    String phone;
    Boolean isItSelf;
    Integer numberOfApplicant;
    AccommodationType accommodationType;
    Boolean visaNeeded;
    Boolean visaFollowByGes;
    EmergencyContact emergencyContact;
    PaymentPreference paymentPreference;
    Boolean under18;
    String parentFullName;
    String parentPhoneNumber;
    String parentEmailAddress;
    String parentRelationship;
    String userNotes;
    String followerPerson;
    String notes;
    BigDecimal priceAmount;
    String priceCurrency;
}

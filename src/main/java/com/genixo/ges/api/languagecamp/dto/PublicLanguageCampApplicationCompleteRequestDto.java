package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Value;

@Value
public class PublicLanguageCampApplicationCompleteRequestDto {

    @Valid
    @NotNull
    Account account;

    @Valid
    @NotNull
    ApplicantProfile applicantProfile;

    @Valid
    @NotNull
    Application application;

    @Value
    public static class Account {
        @Email
        @NotBlank
        String email;

        @NotBlank
        @Size(min = 8, max = 72)
        String password;
    }

    @Value
    public static class ApplicantProfile {
        String firstName;
        String lastName;
        LocalDate birthDate;
        String phone;
        String nationality;

        @Valid
        Address address;
    }

    @Value
    public static class Application {
        @NotNull
        LanguageCampCategory category;

        AccommodationType accommodationType;
        Boolean visaNeeded;
        Boolean visaFollowByGes;
        EmergencyContact emergencyContact;
        PaymentPreference paymentPreference;
        String companyCode;

        Boolean isItSelf;
        Integer numberOfApplicant;

        Boolean under18;
        String parentFullName;
        String parentPhoneNumber;
        String parentEmailAddress;
        String parentRelationship;
        String userNotes;

        /** If true, kvkkAcceptedAt will be set to now. */
        Boolean kvkkAccepted;

        @Valid
        Address invoiceAddress;
    }
}


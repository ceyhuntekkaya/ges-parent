package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.AccommodationType;
import com.genixo.ges.languagecamp.model.EmergencyContact;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import com.genixo.ges.languagecamp.model.PaymentPreference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampApplicationDetailDto {
    UUID id;
    ApplicationStatus status;
    LanguageCampCategory category;

    UUID languageCampProjectId;
    String languageCampProjectTitle;
    AccommodationType accommodationType;
    Boolean visaNeeded;
    Boolean visaFollowByGes;
    EmergencyContact emergencyContact;
    PaymentPreference paymentPreference;
    boolean paymentCompleted;
    List<LanguageCampApplicationPaymentDto> payments;
    BigDecimal priceAmount;
    String priceCurrency;
    Instant kvkkAcceptedAt;

    UUID companyId;
    CompanyDto company;

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
    String followerPerson;
    String notes;

    List<LanguageCampApplicationNoteDto> applicationNotes;
    List<LanguageCampApplicationMeetingDto> meetings;
    List<LanguageCampApplicationTaskDto> tasks;
    List<LanguageCampApplicationDocumentDto> documents;

    LanguageCampVisaFormDto visaForm;

    UUID applicantUserId;
    String applicantEmail;
    String applicantDisplayName;
    Integer participantIndex;
    Integer participantCount;
    List<LanguageCampApplicationGroupParticipantSummaryDto> groupParticipants;

    Instant createdAt;
    Instant updatedAt;
}


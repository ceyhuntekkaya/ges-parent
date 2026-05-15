package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.StartTermSeason;
import com.genixo.ges.university.model.UniversityAccommodationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationDetailDto {
    UUID id;
    /** Başvuruyu portalda görüntüleyen kullanıcı (USER) hesabı. */
    UUID applicantUserId;
    String applicantEmail;
    ApplicationStatus status;
    EducationLevel educationLevel;

    String firstName;
    String lastName;
    LocalDate birthDate;
    String phone;
    String nationality;
    String address;
    String currentSchool;
    Boolean student;
    String classLevel;
    String referencePerson;
    Boolean consultancy;
    String followerPerson;

    List<String> departmentPreferences;
    List<String> countryPreferences;
    List<String> universityPreferences;

    StartTermSeason startTermSeason;
    Integer startYear;

    BigDecimal yearlyBudgetMin;
    BigDecimal yearlyBudgetMax;

    Boolean scholarshipRequested;
    String scholarshipType;

    UniversityAccommodationType accommodationType;

    BigDecimal priceAmount;
    String priceCurrency;

    String notes;
    Instant preferencesCompletedAt;

    List<UniversityApplicationNoteDto> applicationNotes;
    List<UniversityApplicationMeetingDto> meetings;
    List<UniversityApplicationTaskDto> tasks;
    List<UniversityApplicationDocumentDto> documents;
    List<UniversityApplicationPortfolioSectionDto> portfolioSections;
    List<UniversityApplicationPaymentDto> payments;

    Instant createdAt;
    Instant updatedAt;
}


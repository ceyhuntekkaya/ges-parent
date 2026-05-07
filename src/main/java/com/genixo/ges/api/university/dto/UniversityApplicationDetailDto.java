package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.StartTermSeason;
import com.genixo.ges.university.model.UniversityAccommodationType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationDetailDto {
    UUID id;
    ApplicationStatus status;
    EducationLevel educationLevel;

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

    String notes;
    Instant preferencesCompletedAt;

    Instant createdAt;
    Instant updatedAt;
}


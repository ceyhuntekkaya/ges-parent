package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.StartTermSeason;
import com.genixo.ges.university.model.UniversityAccommodationType;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationUpdateRequestDto {
    EducationLevel educationLevel;

    @Size(max = 3)
    List<String> departmentPreferences;

    @Size(max = 5)
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
}


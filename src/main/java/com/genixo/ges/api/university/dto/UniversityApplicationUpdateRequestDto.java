package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.StartTermSeason;
import com.genixo.ges.university.model.UniversityAccommodationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin tarafında tekil alan güncelleme isteği.
 *
 * Not: Spring/Jackson request body deserialize edebilmesi için default constructor + setter gerekir.
 * Lombok @Value immutable olduğu için Jackson creator bulamayıp 500 üretir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationUpdateRequestDto {
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

    EducationLevel educationLevel;

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
}


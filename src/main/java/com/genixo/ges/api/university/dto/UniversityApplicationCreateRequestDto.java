package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.EducationLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UniversityApplicationCreateRequestDto {
    @NotNull
    EducationLevel educationLevel;
}


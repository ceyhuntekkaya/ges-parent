package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.EducationLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityApplicationCreateRequestDto {
    @NotNull
    EducationLevel educationLevel;
}


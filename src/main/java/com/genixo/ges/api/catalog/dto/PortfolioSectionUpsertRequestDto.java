package com.genixo.ges.api.catalog.dto;

import com.genixo.ges.university.model.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSectionUpsertRequestDto {
    @NotBlank
    String name;

    String description;

    EducationLevel educationLevel;

    String departmentKeyword;

    @NotNull
    Integer sortOrder;

    @NotNull
    Boolean defaultRequired;

    Boolean active;
}

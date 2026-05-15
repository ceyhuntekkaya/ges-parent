package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.PortfolioFileType;
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
public class UniversityApplicationPortfolioFileUpsertRequestDto {
    @NotNull
    PortfolioFileType type;

    @NotBlank
    String name;

    String description;

    @NotBlank
    String fileUrl;
}


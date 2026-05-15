package com.genixo.ges.api.university.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationPortfolioSectionUpsertRequestDto {
    /**
     * Katalogdan seçilen portfolyo bölüm şablonu.
     * Null gönderilirse sadece override alanlarıyla "custom" bölüm oluşturulur.
     */
    UUID portfolioSectionId;

    @NotNull
    Boolean required;

    @NotNull
    Integer sortOrder;

    String sectionNameOverride;
    String sectionDescriptionOverride;
}


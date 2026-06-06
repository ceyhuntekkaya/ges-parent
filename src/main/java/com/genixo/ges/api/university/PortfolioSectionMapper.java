package com.genixo.ges.api.university;

import com.genixo.ges.api.university.dto.PortfolioSectionDto;
import com.genixo.ges.university.model.PortfolioSection;

public final class PortfolioSectionMapper {

    private PortfolioSectionMapper() {}

    public static PortfolioSectionDto toDto(PortfolioSection section) {
        if (section == null) {
            return null;
        }
        return PortfolioSectionDto.builder()
            .id(section.getId())
            .name(section.getName())
            .description(section.getDescription())
            .educationLevel(section.getEducationLevel())
            .departmentKeyword(section.getDepartmentKeyword())
            .sortOrder(section.getSortOrder())
            .defaultRequired(section.getDefaultRequired())
            .active(section.getActive())
            .createdAt(section.getCreatedAt())
            .updatedAt(section.getUpdatedAt())
            .build();
    }
}

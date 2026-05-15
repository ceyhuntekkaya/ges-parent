package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectDetailDto;
import com.genixo.ges.languagecamp.model.LanguageCampProject;

final class LanguageCampProjectDtoMapper {

    private LanguageCampProjectDtoMapper() {}

    static LanguageCampProjectDetailDto toDetailDto(LanguageCampProject p) {
        return LanguageCampProjectDetailDto.builder()
            .id(p.getId())
            .title(p.getTitle())
            .companyId(p.getCompany() == null ? null : p.getCompany().getId())
            .quota(p.getQuota())
            .applicationStartAt(p.getApplicationStartAt())
            .applicationEndAt(p.getApplicationEndAt())
            .projectStartAt(p.getProjectStartAt())
            .projectEndAt(p.getProjectEndAt())
            .projectStatus(p.getProjectStatus())
            .projectType(p.getProjectType())
            .banner(LanguageCampProjectMediaUrls.toPublicMediaUrl(p.getBanner()))
            .smallBanner(LanguageCampProjectMediaUrls.toPublicMediaUrl(p.getSmallBanner()))
            .images(p.getImages() == null ? null : p.getImages().stream().map(LanguageCampProjectMediaUrls::toPublicMediaUrl).toList())
            .presentationVideoUrl(p.getPresentationVideoUrl())
            .presentationDocumentUrl(p.getPresentationDocumentUrl())
            .description(p.getDescription())
            .duration(p.getDuration())
            .primaryLocations(p.getPrimaryLocations())
            .locations(p.getLocations())
            .location(p.getLocation())
            .price(p.getPrice())
            .originalPrice(p.getOriginalPrice())
            .currency(p.getCurrency())
            .included(p.getIncluded())
            .excluded(p.getExcluded())
            .highlights(p.getHighlights())
            .itinerary(p.getItinerary())
            .allowParent(p.getAllowParent())
            .allowTeacher(p.getAllowTeacher())
            .allowManager(p.getAllowManager())
            .individual(p.getIndividual())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }
}

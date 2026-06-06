package com.genixo.ges.portal;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.portal.dto.PortalHomePageContentDto;
import com.genixo.ges.api.portal.dto.PortalHomePageContentUpdateRequestDto;
import com.genixo.ges.portal.model.PortalHomePageContent;
import com.genixo.ges.portal.repo.PortalHomePageContentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortalHomePageContentService {

    private final PortalHomePageContentRepository content;

    public PortalHomePageContentService(PortalHomePageContentRepository content) {
        this.content = content;
    }

    @Transactional(readOnly = true)
    public PortalHomePageContentDto getSingleton() {
        return toDto(requireSingleton());
    }

    @Transactional
    public PortalHomePageContentDto update(PortalHomePageContentUpdateRequestDto req) {
        PortalHomePageContent row = requireSingleton();
        apply(row, req);
        content.save(row);
        return toDto(row);
    }

    private PortalHomePageContent requireSingleton() {
        return content.findById(PortalHomePageContent.SINGLETON_ID)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Home page content not found"));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    private static void apply(PortalHomePageContent row, PortalHomePageContentUpdateRequestDto req) {
        row.setBadgeText(req.getBadgeText().trim());
        row.setHeroTitle(req.getHeroTitle().trim());
        row.setHeroDescription(req.getHeroDescription().trim());
        row.setHeroPrimaryCtaText(req.getHeroPrimaryCtaText().trim());
        row.setHeroSecondaryCtaText(req.getHeroSecondaryCtaText().trim());
        row.setFeature1Title(req.getFeature1Title().trim());
        row.setFeature1Description(req.getFeature1Description().trim());
        row.setFeature2Title(req.getFeature2Title().trim());
        row.setFeature2Description(req.getFeature2Description().trim());
        row.setFeature3Title(req.getFeature3Title().trim());
        row.setFeature3Description(req.getFeature3Description().trim());
        row.setHeroImageUrl(trimToNull(req.getHeroImageUrl()));
        row.setSidebarImage1Url(trimToNull(req.getSidebarImage1Url()));
        row.setSidebarImage2Url(trimToNull(req.getSidebarImage2Url()));
        row.setSidebarCardTitle(req.getSidebarCardTitle().trim());
        row.setSidebarCardDescription(req.getSidebarCardDescription().trim());
        row.setSidebarCardPrimaryCtaText(req.getSidebarCardPrimaryCtaText().trim());
        row.setSidebarCardSecondaryCtaText(req.getSidebarCardSecondaryCtaText().trim());
        row.setAboutSectionLabel(req.getAboutSectionLabel().trim());
        row.setAboutSectionTitle(req.getAboutSectionTitle().trim());
        row.setAboutSectionCtaText(req.getAboutSectionCtaText().trim());
        row.setAboutFeature1Title(req.getAboutFeature1Title().trim());
        row.setAboutFeature1Description(req.getAboutFeature1Description().trim());
        row.setAboutFeature2Title(req.getAboutFeature2Title().trim());
        row.setAboutFeature2Description(req.getAboutFeature2Description().trim());
        row.setAboutFeature3Title(req.getAboutFeature3Title().trim());
        row.setAboutFeature3Description(req.getAboutFeature3Description().trim());
        row.setProcessSectionTitle(req.getProcessSectionTitle().trim());
        row.setProcessStep1Title(req.getProcessStep1Title().trim());
        row.setProcessStep1Description(req.getProcessStep1Description().trim());
        row.setProcessStep2Title(req.getProcessStep2Title().trim());
        row.setProcessStep2Description(req.getProcessStep2Description().trim());
        row.setProcessStep3Title(req.getProcessStep3Title().trim());
        row.setProcessStep3Description(req.getProcessStep3Description().trim());
        row.setProcessImageUrl(trimToNull(req.getProcessImageUrl()));
        row.setProcessImageCaption(trimToNull(req.getProcessImageCaption()));
        row.setGallerySectionLabel(req.getGallerySectionLabel().trim());
        row.setGallerySectionTitle(req.getGallerySectionTitle().trim());
        row.setGallerySectionCtaText(req.getGallerySectionCtaText().trim());
        row.setGalleryImage1Url(trimToNull(req.getGalleryImage1Url()));
        row.setGalleryImage2Url(trimToNull(req.getGalleryImage2Url()));
        row.setGalleryImage3Url(trimToNull(req.getGalleryImage3Url()));
        row.setGalleryImage4Url(trimToNull(req.getGalleryImage4Url()));
        row.setGalleryImage5Url(trimToNull(req.getGalleryImage5Url()));
        row.setGalleryImage6Url(trimToNull(req.getGalleryImage6Url()));
    }

    private static PortalHomePageContentDto toDto(PortalHomePageContent row) {
        return PortalHomePageContentDto.builder()
            .id(row.getId())
            .badgeText(row.getBadgeText())
            .heroTitle(row.getHeroTitle())
            .heroDescription(row.getHeroDescription())
            .heroPrimaryCtaText(row.getHeroPrimaryCtaText())
            .heroSecondaryCtaText(row.getHeroSecondaryCtaText())
            .feature1Title(row.getFeature1Title())
            .feature1Description(row.getFeature1Description())
            .feature2Title(row.getFeature2Title())
            .feature2Description(row.getFeature2Description())
            .feature3Title(row.getFeature3Title())
            .feature3Description(row.getFeature3Description())
            .heroImageUrl(row.getHeroImageUrl())
            .sidebarImage1Url(row.getSidebarImage1Url())
            .sidebarImage2Url(row.getSidebarImage2Url())
            .sidebarCardTitle(row.getSidebarCardTitle())
            .sidebarCardDescription(row.getSidebarCardDescription())
            .sidebarCardPrimaryCtaText(row.getSidebarCardPrimaryCtaText())
            .sidebarCardSecondaryCtaText(row.getSidebarCardSecondaryCtaText())
            .aboutSectionLabel(row.getAboutSectionLabel())
            .aboutSectionTitle(row.getAboutSectionTitle())
            .aboutSectionCtaText(row.getAboutSectionCtaText())
            .aboutFeature1Title(row.getAboutFeature1Title())
            .aboutFeature1Description(row.getAboutFeature1Description())
            .aboutFeature2Title(row.getAboutFeature2Title())
            .aboutFeature2Description(row.getAboutFeature2Description())
            .aboutFeature3Title(row.getAboutFeature3Title())
            .aboutFeature3Description(row.getAboutFeature3Description())
            .processSectionTitle(row.getProcessSectionTitle())
            .processStep1Title(row.getProcessStep1Title())
            .processStep1Description(row.getProcessStep1Description())
            .processStep2Title(row.getProcessStep2Title())
            .processStep2Description(row.getProcessStep2Description())
            .processStep3Title(row.getProcessStep3Title())
            .processStep3Description(row.getProcessStep3Description())
            .processImageUrl(row.getProcessImageUrl())
            .processImageCaption(row.getProcessImageCaption())
            .gallerySectionLabel(row.getGallerySectionLabel())
            .gallerySectionTitle(row.getGallerySectionTitle())
            .gallerySectionCtaText(row.getGallerySectionCtaText())
            .galleryImage1Url(row.getGalleryImage1Url())
            .galleryImage2Url(row.getGalleryImage2Url())
            .galleryImage3Url(row.getGalleryImage3Url())
            .galleryImage4Url(row.getGalleryImage4Url())
            .galleryImage5Url(row.getGalleryImage5Url())
            .galleryImage6Url(row.getGalleryImage6Url())
            .createdAt(row.getCreatedAt())
            .updatedAt(row.getUpdatedAt())
            .build();
    }
}

package com.genixo.ges.api.portal.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PortalHomePageContentDto {
    UUID id;
    String badgeText;
    String heroTitle;
    String heroDescription;
    String heroPrimaryCtaText;
    String heroSecondaryCtaText;
    String feature1Title;
    String feature1Description;
    String feature2Title;
    String feature2Description;
    String feature3Title;
    String feature3Description;
    String heroImageUrl;
    String sidebarImage1Url;
    String sidebarImage2Url;
    String sidebarCardTitle;
    String sidebarCardDescription;
    String sidebarCardPrimaryCtaText;
    String sidebarCardSecondaryCtaText;
    String aboutSectionLabel;
    String aboutSectionTitle;
    String aboutSectionCtaText;
    String aboutFeature1Title;
    String aboutFeature1Description;
    String aboutFeature2Title;
    String aboutFeature2Description;
    String aboutFeature3Title;
    String aboutFeature3Description;
    String processSectionTitle;
    String processStep1Title;
    String processStep1Description;
    String processStep2Title;
    String processStep2Description;
    String processStep3Title;
    String processStep3Description;
    String processImageUrl;
    String processImageCaption;
    String gallerySectionLabel;
    String gallerySectionTitle;
    String gallerySectionCtaText;
    String galleryImage1Url;
    String galleryImage2Url;
    String galleryImage3Url;
    String galleryImage4Url;
    String galleryImage5Url;
    String galleryImage6Url;
    Instant createdAt;
    Instant updatedAt;
}

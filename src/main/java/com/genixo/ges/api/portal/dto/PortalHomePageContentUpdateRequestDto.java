package com.genixo.ges.api.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin ana sayfa güncelleme isteği.
 * Jackson request body deserialize için default constructor + setter gerekir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortalHomePageContentUpdateRequestDto {
    @NotBlank
    @Size(max = 255)
    private String badgeText;

    @NotBlank
    @Size(max = 512)
    private String heroTitle;

    @NotBlank
    private String heroDescription;

    @NotBlank
    @Size(max = 128)
    private String heroPrimaryCtaText;

    @NotBlank
    @Size(max = 128)
    private String heroSecondaryCtaText;

    @NotBlank
    @Size(max = 128)
    private String feature1Title;

    @NotBlank
    private String feature1Description;

    @NotBlank
    @Size(max = 128)
    private String feature2Title;

    @NotBlank
    private String feature2Description;

    @NotBlank
    @Size(max = 128)
    private String feature3Title;

    @NotBlank
    private String feature3Description;

    @Size(max = 1024)
    private String heroImageUrl;

    @Size(max = 1024)
    private String sidebarImage1Url;

    @Size(max = 1024)
    private String sidebarImage2Url;

    @NotBlank
    @Size(max = 255)
    private String sidebarCardTitle;

    @NotBlank
    private String sidebarCardDescription;

    @NotBlank
    @Size(max = 128)
    private String sidebarCardPrimaryCtaText;

    @NotBlank
    @Size(max = 128)
    private String sidebarCardSecondaryCtaText;

    @NotBlank
    @Size(max = 128)
    private String aboutSectionLabel;

    @NotBlank
    @Size(max = 255)
    private String aboutSectionTitle;

    @NotBlank
    @Size(max = 128)
    private String aboutSectionCtaText;

    @NotBlank
    @Size(max = 128)
    private String aboutFeature1Title;

    @NotBlank
    private String aboutFeature1Description;

    @NotBlank
    @Size(max = 128)
    private String aboutFeature2Title;

    @NotBlank
    private String aboutFeature2Description;

    @NotBlank
    @Size(max = 128)
    private String aboutFeature3Title;

    @NotBlank
    private String aboutFeature3Description;

    @NotBlank
    @Size(max = 255)
    private String processSectionTitle;

    @NotBlank
    @Size(max = 128)
    private String processStep1Title;

    @NotBlank
    private String processStep1Description;

    @NotBlank
    @Size(max = 128)
    private String processStep2Title;

    @NotBlank
    private String processStep2Description;

    @NotBlank
    @Size(max = 128)
    private String processStep3Title;

    @NotBlank
    private String processStep3Description;

    @Size(max = 1024)
    private String processImageUrl;

    private String processImageCaption;

    @NotBlank
    @Size(max = 128)
    private String gallerySectionLabel;

    @NotBlank
    @Size(max = 255)
    private String gallerySectionTitle;

    @NotBlank
    @Size(max = 128)
    private String gallerySectionCtaText;

    @Size(max = 1024)
    private String galleryImage1Url;

    @Size(max = 1024)
    private String galleryImage2Url;

    @Size(max = 1024)
    private String galleryImage3Url;

    @Size(max = 1024)
    private String galleryImage4Url;

    @Size(max = 1024)
    private String galleryImage5Url;

    @Size(max = 1024)
    private String galleryImage6Url;
}

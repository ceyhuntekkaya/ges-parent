package com.genixo.ges.portal.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "portal_home_page_content")
public class PortalHomePageContent extends BaseEntity {

    public static final UUID SINGLETON_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @Column(nullable = false, length = 255)
    private String badgeText;

    @Column(nullable = false, length = 512)
    private String heroTitle;

    @Column(nullable = false, columnDefinition = "text")
    private String heroDescription;

    @Column(nullable = false, length = 128)
    private String heroPrimaryCtaText;

    @Column(nullable = false, length = 128)
    private String heroSecondaryCtaText;

    @Column(nullable = false, length = 128)
    private String feature1Title;

    @Column(nullable = false, columnDefinition = "text")
    private String feature1Description;

    @Column(nullable = false, length = 128)
    private String feature2Title;

    @Column(nullable = false, columnDefinition = "text")
    private String feature2Description;

    @Column(nullable = false, length = 128)
    private String feature3Title;

    @Column(nullable = false, columnDefinition = "text")
    private String feature3Description;

    @Column(length = 1024)
    private String heroImageUrl;

    @Column(length = 1024)
    private String sidebarImage1Url;

    @Column(length = 1024)
    private String sidebarImage2Url;

    @Column(nullable = false, length = 255)
    private String sidebarCardTitle;

    @Column(nullable = false, columnDefinition = "text")
    private String sidebarCardDescription;

    @Column(nullable = false, length = 128)
    private String sidebarCardPrimaryCtaText;

    @Column(nullable = false, length = 128)
    private String sidebarCardSecondaryCtaText;

    @Column(nullable = false, length = 128)
    private String aboutSectionLabel;

    @Column(nullable = false, length = 255)
    private String aboutSectionTitle;

    @Column(nullable = false, length = 128)
    private String aboutSectionCtaText;

    @Column(nullable = false, length = 128)
    private String aboutFeature1Title;

    @Column(nullable = false, columnDefinition = "text")
    private String aboutFeature1Description;

    @Column(nullable = false, length = 128)
    private String aboutFeature2Title;

    @Column(nullable = false, columnDefinition = "text")
    private String aboutFeature2Description;

    @Column(nullable = false, length = 128)
    private String aboutFeature3Title;

    @Column(nullable = false, columnDefinition = "text")
    private String aboutFeature3Description;

    @Column(nullable = false, length = 255)
    private String processSectionTitle;

    @Column(nullable = false, length = 128)
    private String processStep1Title;

    @Column(nullable = false, columnDefinition = "text")
    private String processStep1Description;

    @Column(nullable = false, length = 128)
    private String processStep2Title;

    @Column(nullable = false, columnDefinition = "text")
    private String processStep2Description;

    @Column(nullable = false, length = 128)
    private String processStep3Title;

    @Column(nullable = false, columnDefinition = "text")
    private String processStep3Description;

    @Column(length = 1024)
    private String processImageUrl;

    @Column(columnDefinition = "text")
    private String processImageCaption;

    @Column(nullable = false, length = 128)
    private String gallerySectionLabel;

    @Column(nullable = false, length = 255)
    private String gallerySectionTitle;

    @Column(nullable = false, length = 128)
    private String gallerySectionCtaText;

    @Column(length = 1024)
    private String galleryImage1Url;

    @Column(length = 1024)
    private String galleryImage2Url;

    @Column(length = 1024)
    private String galleryImage3Url;

    @Column(length = 1024)
    private String galleryImage4Url;

    @Column(length = 1024)
    private String galleryImage5Url;

    @Column(length = 1024)
    private String galleryImage6Url;
}

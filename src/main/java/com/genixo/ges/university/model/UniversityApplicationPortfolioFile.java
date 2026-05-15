package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "university_application_portfolio_files")
public class UniversityApplicationPortfolioFile extends BaseEntity {

    /** Dosyanın bağlı olduğu başvuru portfolyo bölümü. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_portfolio_section_id", nullable = false)
    private UniversityApplicationPortfolioSection portfolioSection;

    /** Dosya tipi (resim/video/ses/pdf/diğer). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PortfolioFileType type = PortfolioFileType.OTHER;

    /** Dosya adı (gösterim adı). */
    @Column(nullable = false, length = 256)
    private String name;

    /** Dosya açıklaması (opsiyonel). */
    @Column(columnDefinition = "text")
    private String description;

    /** Dosyanın saklandığı adres (URL/path). */
    @Column(nullable = false, length = 1024)
    private String fileUrl;
}


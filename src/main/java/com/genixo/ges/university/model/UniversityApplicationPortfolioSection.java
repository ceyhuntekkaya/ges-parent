package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "university_application_portfolio_sections")
public class UniversityApplicationPortfolioSection extends BaseEntity {

    /** Bu portfolyo bölümünün bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /**
     * Re-usable template section (selectable from a catalog).
     * The per-application required flag can vary across applications.
     */
    /** Katalogdan seçilen portfolyo bölüm şablonu (opsiyonel). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_section_id")
    private PortfolioSection portfolioSection;

    /** Bu başvuru için bu bölümün zorunlu olup olmadığı. */
    @Column(nullable = false)
    private Boolean required = Boolean.FALSE;

    /** Başvuru bazında bölüm adı override (template yerine farklı isim göstermek için). */
    @Column(length = 128)
    private String sectionNameOverride;

    /** Başvuru bazında bölüm açıklaması override. */
    @Column(columnDefinition = "text")
    private String sectionDescriptionOverride;

    /** Başvuru ekranlarında bölüm sıralaması. */
    @Column(nullable = false)
    private Integer sortOrder = 0;

    /** Bu bölüme yüklenen dosyalar listesi. */
    @OneToMany(
            mappedBy = "portfolioSection",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id asc")
    private List<UniversityApplicationPortfolioFile> files;
}


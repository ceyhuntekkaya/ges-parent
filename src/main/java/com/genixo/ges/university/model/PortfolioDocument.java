package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.storage.model.StoredFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "portfolio_documents")
public class PortfolioDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_application_id", nullable = false)
    private UniversityApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PortfolioDocumentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PortfolioDocumentType type = PortfolioDocumentType.OTHER;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String relatedProgram; // motivasyon mektubu gibi program bazlı bağlama

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private StoredFile file;

    @Column(length = 1024)
    private String externalUrl; // github/portfolio link vs.
}


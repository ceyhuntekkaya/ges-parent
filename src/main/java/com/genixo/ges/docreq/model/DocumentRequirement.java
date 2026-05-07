package com.genixo.ges.docreq.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_requirements")
public class DocumentRequirement extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DocumentRequirementScope scope;

    @Column(length = 32)
    private String category; // e.g. LANGUAGE_CAMP: INDIVIDUAL/CORPORATE/FAMILY

    @Column(name = "requirement_key", nullable = false, length = 128)
    private String key; // stable identifier used by UI/backoffice (slot)

    @Column(nullable = false)
    private boolean required;

    @Column(length = 255)
    private String allowedContentTypes; // e.g. "application/pdf,image/jpeg,image/png"

    @Column(nullable = false)
    private long maxSizeBytes = 20L * 1024 * 1024; // PDF notunda 20MB / dosya

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}


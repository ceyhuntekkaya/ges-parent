package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_application_documents")
public class LanguageCampApplicationDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LanguageCampApplication application;

    @Column(nullable = false)
    private Boolean required = Boolean.FALSE;

    @Column(nullable = false, length = 128)
    private String documentName;

    @Column(columnDefinition = "text")
    private String documentDescription;

    @Column(length = 1024)
    private String documentUrl;

    @Column
    private Instant uploadedAt;
}

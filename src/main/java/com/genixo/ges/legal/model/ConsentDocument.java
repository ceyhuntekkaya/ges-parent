package com.genixo.ges.legal.model;

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
@Table(name = "consent_documents")
public class ConsentDocument extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConsentType type;

    @Column(nullable = false, length = 16)
    private String language; // tr/en

    @Column(nullable = false, length = 32)
    private String version; // e.g. 2026-05-07-v1

    @Column(nullable = false)
    private boolean active = true;

    @Column(columnDefinition = "text", nullable = false)
    private String text;
}


package com.genixo.ges.university.model;

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
@Table(name = "portfolio_sections")
public class PortfolioSection extends BaseEntity {

    /** Portfolyo bölüm adı (katalog/template). */
    @Column(nullable = false, length = 128)
    private String name;

    /** Portfolyo bölüm açıklaması (katalog/template). */
    @Column(columnDefinition = "text")
    private String description;

    /** Null ise tüm eğitim seviyelerine uygulanır. */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private EducationLevel educationLevel;

    /** Bölüm tercihlerinde aranacak anahtar kelime (null/boş = tüm bölümler). */
    @Column(length = 128)
    private String departmentKeyword;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean defaultRequired = Boolean.FALSE;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;
}


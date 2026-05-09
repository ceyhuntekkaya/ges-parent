package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.company.model.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_projects")
public class LanguageCampProject extends BaseEntity {

    /**
     * Okul/şirket-spesifik proje adı.
     */
    @Column(unique = true, nullable = false, length = 256)
    private String title;

    /**
     * Bu proje hangi şirkete ait.
     * individual=true ise null (bireysel); individual=false ise kurumsal (genelde dolu).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // ==================== PROJECT SPECIFIC FIELDS ====================

    private Integer quota;

    private Instant applicationStartAt;
    private Instant applicationEndAt;

    private Instant projectStartAt;
    private Instant projectEndAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private EProjectStatus projectStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private EProjectType projectType;

    // ==================== MEDIA & VISUALS (OVERRIDE) ====================

    @Column(length = 1024)
    private String banner;

    @Column(length = 1024)
    private String smallBanner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images;

    @Column(length = 1024)
    private String presentationVideoUrl;

    @Column(length = 1024)
    private String presentationDocumentUrl;

    // ==================== BASIC INFO (OVERRIDE) ====================

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 64)
    private String duration;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> primaryLocations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> locations;

    @Column(length = 256)
    private String location;

    // ==================== PRICING (OVERRIDE) ====================

    private BigDecimal price;
    private BigDecimal originalPrice;

    @Column(length = 8)
    private String currency;

    // ==================== TOUR DETAILS (OVERRIDE) ====================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> included;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> excluded;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> highlights;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> itinerary;

    // ==================== PERMISSIONS (OVERRIDE) ====================

    private Boolean allowParent;
    private Boolean allowTeacher;
    private Boolean allowManager;

    private Boolean individual;
}


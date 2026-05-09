package com.genixo.ges.university.model;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "university_applications")
public class UniversityApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private UserAccount applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_profile_id")
    private ApplicantProfile applicantProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EducationLevel educationLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> departmentPreferences; // 1-3

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> countryPreferences; // 1-5 sıralı

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> universityPreferences;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private StartTermSeason startTermSeason;

    private Integer startYear;

    private BigDecimal yearlyBudgetMin;
    private BigDecimal yearlyBudgetMax;

    private Boolean scholarshipRequested;

    @Column(length = 128)
    private String scholarshipType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private UniversityAccommodationType accommodationType;

    @Column(columnDefinition = "text")
    private String notes;

    private Instant preferencesCompletedAt;


}


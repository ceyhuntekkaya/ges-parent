package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.program.model.Program;
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
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_participants")
public class LanguageCampParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LanguageCampApplication application;

    @Column(nullable = false, length = 64)
    private String firstName;

    @Column(nullable = false, length = 64)
    private String lastName;

    private LocalDate birthDate;

    @Column(length = 128)
    private String nationality;

    @Column(length = 64)
    private String identityNumber; // TC Kimlik veya yabancı kimlik

    @Column(length = 16)
    private String passportSeries;

    @Column(length = 32)
    private String passportNumber;

    private LocalDate passportExpiryDate;

    @Column(columnDefinition = "text")
    private String allergiesAndHealth;

    @Column(columnDefinition = "text")
    private String medicationUsage;

    private Boolean under18;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_consent_file_id")
    private StoredFile guardianConsentFile; // kişi bazlı (aile/grup için)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program; // ailede kişi bazlı bağımsız olabilir

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AccommodationType accommodationType; // kişi bazlı override

    private Boolean visaNeeded;
    private Boolean visaFollowByGes;
}


package com.genixo.ges.languagecamp.model;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.company.model.Company;
import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.program.model.Program;
import com.genixo.ges.storage.model.StoredFile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_applications")
public class LanguageCampApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private UserAccount applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_profile_id")
    private ApplicantProfile applicantProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LanguageCampCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private Program program;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AccommodationType accommodationType;

    private Boolean visaNeeded;
    private Boolean visaFollowByGes;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "emergency_contact_full_name", length = 128)),
            @AttributeOverride(name = "phone", column = @Column(name = "emergency_contact_phone", length = 32)),
            @AttributeOverride(name = "relationship", column = @Column(name = "emergency_contact_relationship", length = 64))
    })
    private EmergencyContact emergencyContact;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private PaymentPreference paymentPreference;

    private Instant kvkkAcceptedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_consent_file_id")
    private StoredFile guardianConsentFile; // 18 yaş altı için (top-level)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "invoice_country", length = 128)),
            @AttributeOverride(name = "city", column = @Column(name = "invoice_city", length = 128)),
            @AttributeOverride(name = "district", column = @Column(name = "invoice_district", length = 128)),
            @AttributeOverride(name = "line1", column = @Column(name = "invoice_line1", length = 512)),
            @AttributeOverride(name = "line2", column = @Column(name = "invoice_line2", length = 512)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "invoice_postal_code", length = 32))
    })
    private Address invoiceAddress;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_participants_file_id")
    private StoredFile bulkParticipantsFile; // Excel listesi (kurumsal)
}


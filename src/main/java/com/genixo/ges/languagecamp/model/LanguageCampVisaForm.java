package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.storage.model.StoredFile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "language_camp_visa_forms")
public class LanguageCampVisaForm extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false, unique = true)
    private LanguageCampParticipant participant;

    @Column(length = 128)
    private String birthPlace;

    @Column(length = 128)
    private String birthCountry;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "res_country", length = 128)),
            @AttributeOverride(name = "city", column = @Column(name = "res_city", length = 128)),
            @AttributeOverride(name = "district", column = @Column(name = "res_district", length = 128)),
            @AttributeOverride(name = "line1", column = @Column(name = "res_line1", length = 512)),
            @AttributeOverride(name = "line2", column = @Column(name = "res_line2", length = 512)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "res_postal_code", length = 32))
    })
    private Address residenceAddress;

    private Boolean visaRejectedBefore;

    @Column(columnDefinition = "text")
    private String visaRejectionDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> visitedCountries;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_statement_file_id")
    private StoredFile bankStatementFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biometric_photo_file_id")
    private StoredFile biometricPhotoFile;

    @Column(length = 128)
    private String appointmentCityPreference;
}


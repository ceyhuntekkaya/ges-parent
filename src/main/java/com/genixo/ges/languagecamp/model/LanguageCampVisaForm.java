package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_visa_forms")
public class LanguageCampVisaForm extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LanguageCampApplication application;

    @Column(length = 64)
    private String passportNumber;

    private LocalDate passportValidUntil;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private PassportType passportType;

    private LocalDate visaValidFrom;

    private LocalDate visaValidUntil;

    @Column(length = 128)
    private String visaIssuingCountry;

    @Column(length = 128)
    private String visaType;

    @OneToMany(mappedBy = "visaForm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LanguageCampVisaFormDocument> documents = new ArrayList<>();
}

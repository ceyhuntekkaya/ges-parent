package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.storage.model.StoredFile;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_visa_form_documents")
public class LanguageCampVisaFormDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visa_form_id", nullable = false)
    private LanguageCampVisaForm visaForm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stored_file_id", nullable = false)
    private StoredFile storedFile;
}

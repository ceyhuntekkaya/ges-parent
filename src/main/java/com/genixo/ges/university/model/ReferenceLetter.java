package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
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
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reference_letters")
public class ReferenceLetter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_application_id", nullable = false)
    private UniversityApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReferenceType referenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReferenceStatus status = ReferenceStatus.NOT_REQUESTED;

    @Column(length = 128)
    private String refereeName;

    @Column(length = 255)
    private String refereeEmail;

    private Instant requestedAt;
    private Instant receivedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private StoredFile file;

    @Column(length = 128)
    private String uploadTokenHash; // referans e-posta ile gönderilecek seçeneği için
}


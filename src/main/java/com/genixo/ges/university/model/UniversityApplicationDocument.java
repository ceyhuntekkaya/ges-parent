package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "university_application_documents")
public class UniversityApplicationDocument extends BaseEntity {

    /** Evrak kaydının bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /** Evrak bu başvuru için zorunlu mu? */
    @Column(nullable = false)
    private Boolean required = Boolean.FALSE;

    /** Belge adı (gösterim adı). */
    @Column(nullable = false, length = 128)
    private String documentName;

    /** Belge açıklaması (opsiyonel). */
    @Column(columnDefinition = "text")
    private String documentDescription;

    /** Belgenin saklandığı adres (URL/path); dosya sonradan eklenebilir. */
    @Column(length = 1024)
    private String documentUrl;

    /** Belgenin yüklendiği zaman; dosya yoksa null. */
    @Column
    private Instant uploadedAt;
}


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
@Table(name = "university_application_notes")
public class UniversityApplicationNote extends BaseEntity {

    /** Notun bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /** Notu yazan kişi (serbest metin; kullanıcı adı/isim). */
    @Column(nullable = false, length = 128)
    private String writtenBy;

    /** Notun yazıldığı zaman. */
    @Column(nullable = false)
    private Instant writtenAt = Instant.now();

    /** Yapılacak metni / not içeriği. */
    @Column(nullable = false, columnDefinition = "text")
    private String todoText;
}


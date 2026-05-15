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
@Table(name = "university_application_tasks")
public class UniversityApplicationTask extends BaseEntity {

    /** İşin/aksiyonun bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /** İşin planlanan tarihi/saati. */
    @Column(nullable = false)
    private Instant scheduledAt;

    /** İşin kimle yapılacağı (serbest metin). */
    @Column(nullable = false, length = 256)
    private String withWhom;

    /** Ne yapılacak? (iş tanımı). */
    @Column(nullable = false, columnDefinition = "text")
    private String whatToDo;

    /** İş durumu (yapıldı/yapılmadı). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UniversityApplicationTaskStatus status = UniversityApplicationTaskStatus.PENDING;

    /** İşi yapan kullanıcı (serbest metin; kullanıcı adı/isim). */
    @Column(length = 128)
    private String performedByUser;
}


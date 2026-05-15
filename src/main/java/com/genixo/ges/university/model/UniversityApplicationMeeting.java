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
@Table(name = "university_application_meetings")
public class UniversityApplicationMeeting extends BaseEntity {

    /** Görüşmenin bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /** Görüşülen kişi (serbest metin). */
    @Column(nullable = false, length = 128)
    private String person;

    /** Görüşme tarihi/saati. */
    @Column(nullable = false)
    private Instant meetingAt;

    /** Görüşme notu (serbest metin). */
    @Column(columnDefinition = "text")
    private String meetingNote;

    /** Görüşme sonucu metni (serbest metin). */
    @Column(columnDefinition = "text")
    private String meetingResult;
}


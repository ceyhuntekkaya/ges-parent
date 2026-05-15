package com.genixo.ges.university.model;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
@Table(name = "university_applications")
public class UniversityApplication extends BaseEntity {

    /** Başvuruyu yapan kullanıcı hesabı. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private UserAccount applicant;

    /** Başvuru sahibinin (opsiyonel) profil kaydı; mevcut profilden otomatik doldurma/bağ kurma için. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_profile_id")
    private ApplicantProfile applicantProfile;

    /** Başvuru sahibinin adı (başvuru anındaki snapshot). */
    @Column(length = 64)
    private String firstName;

    /** Başvuru sahibinin soyadı (başvuru anındaki snapshot). */
    @Column(length = 64)
    private String lastName;

    /** Doğum tarihi (başvuru anındaki snapshot). */
    private LocalDate birthDate;

    /** İletişim telefonu. */
    @Column(length = 32)
    private String phone;

    /** Uyruk / vatandaşlık bilgisi. */
    @Column(length = 128)
    private String nationality;

    /** Adres (serbest metin). */
    @Column(columnDefinition = "text")
    private String address;

    /** Mevcut okul bilgisi (serbest metin). */
    @Column(length = 128)
    private String currentSchool;

    /** Başvuru sahibi öğrenci mi? */
    private Boolean student;

    /** Sınıf seviyesi (örn: 11. sınıf, 2. sınıf vb.) */
    @Column(length = 64)
    private String classLevel;

    /** Referans olan kişi (serbest metin). */
    @Column(length = 128)
    private String referencePerson;

    /** Danışmanlık hizmeti isteniyor/veriliyor mu? */
    private Boolean consultancy;

    /** Takip eden kişi (serbest metin). */
    @Column(length = 128)
    private String followerPerson;

    /** Başvurunun iş akışı durumu. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    /** Başvuru sahibinin eğitim seviyesi. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EducationLevel educationLevel;

    /** Hedef bölüm tercihleri (sıralı liste). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> departmentPreferences; // 1-3

    /** Hedef ülke tercihleri (sıralı liste). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> countryPreferences; // 1-5 sıralı

    /** Hedef üniversite tercihleri (sıralı liste). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> universityPreferences;

    /** Başlamayı hedeflediği dönem mevsimi. */
    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private StartTermSeason startTermSeason;

    /** Başlamayı hedeflediği yıl. */
    private Integer startYear;

    /** Yıllık bütçe alt sınırı. */
    private BigDecimal yearlyBudgetMin;
    /** Yıllık bütçe üst sınırı. */
    private BigDecimal yearlyBudgetMax;

    /** Burs talebi var mı? */
    private Boolean scholarshipRequested;

    /** Burs tipi/tercihi (serbest metin). */
    @Column(length = 256)
    private String scholarshipType;

    /** Konaklama tercihi. */
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private UniversityAccommodationType accommodationType;

    /** Başvuru için belirlenen toplam/ana fiyat tutarı. */
    private BigDecimal priceAmount;

    /** Fiyatın para birimi (örn: TRY, USD, EUR). */
    @Column(length = 8)
    private String priceCurrency;

    /** Başvuru ile ilgili genel not alanı (legacy / serbest metin). */
    @Column(columnDefinition = "text")
    private String notes;

    /** Tercih adımı tamamlandığı an (raporlama/akış için). */
    private Instant preferencesCompletedAt;

    /** Yapılacaklar/notlar listesi (kim yazdı, ne zaman, yapılacak metin). */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("createdAt desc")
    private List<UniversityApplicationNote> applicationNotes;

    /** Görüşme kayıtları (kimle, ne zaman, not/sonuç). */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("meetingAt desc")
    private List<UniversityApplicationMeeting> meetings;

    /** Başvuru işleri/aksiyonları (planlanan tarih, yapılacak iş, durum vb.). */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("scheduledAt asc")
    private List<UniversityApplicationTask> tasks;

    /** Evrak listesi (zorunluluk, dosya adresi, yüklenme zamanı). */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("uploadedAt desc")
    private List<UniversityApplicationDocument> documents;

    /** Başvuruya atanmış portfolyo bölümleri (başvuruya özel required/override + dosyalar). */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder asc, id asc")
    private List<UniversityApplicationPortfolioSection> portfolioSections;

    /** Bu başvuru kapsamında alınan ödemeler listesi. */
    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("paymentAt desc")
    private List<UniversityApplicationPayment> payments;

}


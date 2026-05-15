package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "university_application_payments")
public class UniversityApplicationPayment extends BaseEntity {

    /** Ödemenin bağlı olduğu üniversite başvurusu. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private UniversityApplication application;

    /** Ödeme tarihi/saati. */
    @Column(nullable = false)
    private Instant paymentAt;

    /** Ödeme tutarı. */
    @Column(nullable = false)
    private BigDecimal amount;

    /** Ödemenin para birimi (örn: TRY, USD, EUR). */
    @Column(nullable = false, length = 8)
    private String currency;

    /** Ödemeyi alan kişi (serbest metin). */
    @Column(length = 128)
    private String receivedBy;
}


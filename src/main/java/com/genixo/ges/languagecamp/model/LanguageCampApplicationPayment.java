package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_application_payments")
public class LanguageCampApplicationPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LanguageCampApplication application;

    @Column(nullable = false)
    private Instant paymentAt;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(length = 128)
    private String receivedBy;
}

package com.genixo.ges.company.model;

import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
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
@Table(name = "companies")
public class Company extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private UserAccount owner;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 64)
    private String taxNumber;

    @Column(length = 128)
    private String contactFullName;

    @Column(length = 32)
    private String contactPhone;

    @Column(length = 255)
    private String contactEmail;
}


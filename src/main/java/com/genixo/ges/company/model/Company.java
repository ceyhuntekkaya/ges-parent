package com.genixo.ges.company.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false, length = 64, unique = true)
    private String code;

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


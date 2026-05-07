package com.genixo.ges.common.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Address {

    @Column(length = 128)
    private String country;

    @Column(length = 128)
    private String city;

    @Column(length = 128)
    private String district;

    @Column(length = 512)
    private String line1;

    @Column(length = 512)
    private String line2;

    @Column(length = 32)
    private String postalCode;
}


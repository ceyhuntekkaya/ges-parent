package com.genixo.ges.languagecamp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class EmergencyContact {

    @Column(length = 128)
    private String fullName;

    @Column(length = 32)
    private String phone;

    @Column(length = 64)
    private String relationship;
}


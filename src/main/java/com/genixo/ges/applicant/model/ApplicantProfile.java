package com.genixo.ges.applicant.model;

import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "applicant_profiles")
public class ApplicantProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(length = 64)
    private String firstName;

    @Column(length = 64)
    private String lastName;

    private LocalDate birthDate;

    @Column(length = 32)
    private String phone;

    @Column(length = 128)
    private String nationality;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "addr_country", length = 128)),
            @AttributeOverride(name = "city", column = @Column(name = "addr_city", length = 128)),
            @AttributeOverride(name = "district", column = @Column(name = "addr_district", length = 128)),
            @AttributeOverride(name = "line1", column = @Column(name = "addr_line1", length = 512)),
            @AttributeOverride(name = "line2", column = @Column(name = "addr_line2", length = 512)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "addr_postal_code", length = 32))
    })
    private Address address;
}


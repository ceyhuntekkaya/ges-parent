package com.genixo.ges.api.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CompanyUpsertRequestDto {
    @NotBlank
    @Size(max = 255)
    String name;

    @Size(max = 64)
    String taxNumber;

    @Size(max = 128)
    String contactFullName;

    @Size(max = 32)
    String contactPhone;

    @Email
    @Size(max = 255)
    String contactEmail;
}


package com.genixo.ges.api.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CountryUpsertRequestDto {
    @NotBlank
    @Size(max = 8)
    String code;

    @NotBlank
    @Size(max = 128)
    String name;
}


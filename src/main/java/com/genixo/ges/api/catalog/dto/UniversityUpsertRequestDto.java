package com.genixo.ges.api.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Value;

@Value
public class UniversityUpsertRequestDto {
    @NotNull
    UUID countryId;

    @NotBlank
    @Size(max = 255)
    String name;

    Boolean active;
}


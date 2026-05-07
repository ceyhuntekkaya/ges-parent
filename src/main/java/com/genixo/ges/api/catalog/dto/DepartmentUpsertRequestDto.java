package com.genixo.ges.api.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class DepartmentUpsertRequestDto {
    @NotBlank
    @Size(max = 255)
    String name;

    Boolean active;
}


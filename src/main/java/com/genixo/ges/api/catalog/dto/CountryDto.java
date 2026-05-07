package com.genixo.ges.api.catalog.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CountryDto {
    UUID id;
    String code;
    String name;
}


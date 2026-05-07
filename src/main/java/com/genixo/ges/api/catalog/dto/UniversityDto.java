package com.genixo.ges.api.catalog.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityDto {
    UUID id;
    String name;
    boolean active;
    CountryDto country;
}


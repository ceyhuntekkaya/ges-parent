package com.genixo.ges.api.languagecamp.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompanyDto {
    UUID id;
    String code;
    String name;
    String taxNumber;
    String contactFullName;
    String contactPhone;
    String contactEmail;
    Instant createdAt;
    Instant updatedAt;
}


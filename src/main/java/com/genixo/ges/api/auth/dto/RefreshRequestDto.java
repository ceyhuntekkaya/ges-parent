package com.genixo.ges.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class RefreshRequestDto {
    @NotBlank
    String refreshToken;
}


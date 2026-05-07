package com.genixo.ges.api.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokensDto {
    String tokenType;
    String accessToken;
    Long expiresInSeconds;
    String refreshToken;
}


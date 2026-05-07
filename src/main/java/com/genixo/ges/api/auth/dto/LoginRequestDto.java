package com.genixo.ges.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class LoginRequestDto {
    @Email
    @NotBlank
    String email;

    @NotBlank
    String password;
}


package com.genixo.ges.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.genixo.ges.auth.model.UserRole;
import lombok.Value;

@Value
public class RegisterRequestDto {
    @Email
    @NotBlank
    String email;

    @NotBlank
    @Size(min = 8, max = 72)
    String password;

    @NotNull
    UserRole role;
}


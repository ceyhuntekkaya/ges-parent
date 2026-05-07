package com.genixo.ges.api.auth.dto;

import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.model.UserStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MeDto {
    UUID id;
    String email;
    UserRole role;
    UserStatus status;
}


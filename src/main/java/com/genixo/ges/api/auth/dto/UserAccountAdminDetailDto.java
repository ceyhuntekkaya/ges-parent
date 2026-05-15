package com.genixo.ges.api.auth.dto;

import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.model.UserStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** Admin: kullanıcı + varsa başvuru profili (form ön doldurma). */
@Value
@Builder
public class UserAccountAdminDetailDto {
    UUID id;
    String email;
    UserRole role;
    UserStatus status;
    Instant createdAt;

    String applicantFirstName;
    String applicantLastName;
    LocalDate birthDate;
    String phone;
    String nationality;
    /** Profildeki adresin düz metin özeti (tek satırda birleştirilmiş). */
    String address;
}

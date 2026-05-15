package com.genixo.ges.api.auth.dto;

import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.model.UserStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserAccountAdminListItemDto {
    UUID id;
    String email;
    /** {@link com.genixo.ges.applicant.model.ApplicantProfile} alanlarından (varsa). */
    String applicantFirstName;
    String applicantLastName;
    UserRole role;
    UserStatus status;
    Instant createdAt;
}

package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.EducationLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationAdminCreateRequestDto {
    /** Var olan USER hesabı ile başvuru açılır. {@code newApplicant} ile birlikte kullanılamaz. */
    private UUID applicantUserId;

    @Valid
    private UniversityApplicationNewApplicantRequestDto newApplicant;

    @NotNull
    private EducationLevel educationLevel;

    /** Boş ise taslak (DRAFT) olarak oluşturulur. */
    private ApplicationStatus status;

    @Valid
    private UniversityApplicationUpdateRequestDto initialSnapshot;
}

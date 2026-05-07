package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ApplicationStatusChangeRequestDto {
    @NotNull
    ApplicationStatus status;
}


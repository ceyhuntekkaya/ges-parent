package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusChangeRequestDto {
    @NotNull
    ApplicationStatus status;
}


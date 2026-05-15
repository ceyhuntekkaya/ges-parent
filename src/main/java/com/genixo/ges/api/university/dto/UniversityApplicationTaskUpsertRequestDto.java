package com.genixo.ges.api.university.dto;

import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationTaskUpsertRequestDto {
    @NotNull
    Instant scheduledAt;

    @NotBlank
    String withWhom;

    @NotBlank
    String whatToDo;

    UniversityApplicationTaskStatus status;
}


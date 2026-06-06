package com.genixo.ges.api.languagecamp.dto;

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
public class LanguageCampApplicationTaskUpsertRequestDto {
    @NotNull
    Instant scheduledAt;

    @NotBlank
    String withWhom;

    @NotBlank
    String whatToDo;

    UniversityApplicationTaskStatus status;
}

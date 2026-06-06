package com.genixo.ges.api.languagecamp.dto;

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
public class LanguageCampApplicationMeetingUpsertRequestDto {
    @NotBlank
    String person;

    @NotNull
    Instant meetingAt;

    String meetingNote;
    String meetingResult;
}

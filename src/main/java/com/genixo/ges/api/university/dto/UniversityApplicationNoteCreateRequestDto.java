package com.genixo.ges.api.university.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityApplicationNoteCreateRequestDto {
    @NotBlank
    String todoText;
}


package com.genixo.ges.api.university.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityApplicationNewApplicantRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}

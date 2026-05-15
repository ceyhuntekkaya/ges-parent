package com.genixo.ges.api.university.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityApplicationDocumentUpsertRequestDto {
    Boolean required;

    @NotBlank
    String documentName;

    String documentDescription;

    /** Opsiyonel; önce sadece isim/açıklama ile kayıt, dosya sonra güncellenebilir. */
    String documentUrl;
}


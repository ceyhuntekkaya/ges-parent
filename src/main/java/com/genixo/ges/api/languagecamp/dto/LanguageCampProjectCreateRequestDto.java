package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.EProjectStatus;
import com.genixo.ges.languagecamp.model.EProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LanguageCampProjectCreateRequestDto {
    @NotBlank
    @Size(max = 256)
    String title;

    UUID companyId;
    Integer quota;
    Instant applicationStartAt;
    Instant applicationEndAt;
    Instant projectStartAt;
    Instant projectEndAt;
    EProjectStatus projectStatus;
    EProjectType projectType;

    @Size(max = 1024)
    String banner;
    @Size(max = 1024)
    String smallBanner;
    List<String> images;
    @Size(max = 1024)
    String presentationVideoUrl;
    @Size(max = 1024)
    String presentationDocumentUrl;

    String description;
    @Size(max = 64)
    String duration;
    List<String> primaryLocations;
    List<String> locations;
    @Size(max = 256)
    String location;

    BigDecimal price;
    BigDecimal originalPrice;
    @Size(max = 8)
    String currency;

    List<String> included;
    List<String> excluded;
    List<String> highlights;
    List<Map<String, Object>> itinerary;

    Boolean allowParent;
    Boolean allowTeacher;
    Boolean allowManager;

    Boolean individual;
}


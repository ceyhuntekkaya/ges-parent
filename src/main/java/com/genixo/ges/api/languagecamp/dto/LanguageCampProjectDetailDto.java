package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.EProjectStatus;
import com.genixo.ges.languagecamp.model.EProjectType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampProjectDetailDto {
    UUID id;
    String title;
    UUID companyId;
    Integer quota;
    Instant applicationStartAt;
    Instant applicationEndAt;
    Instant projectStartAt;
    Instant projectEndAt;
    EProjectStatus projectStatus;
    EProjectType projectType;
    String banner;
    String smallBanner;
    List<String> images;
    String presentationVideoUrl;
    String presentationDocumentUrl;
    String description;
    String duration;
    List<String> primaryLocations;
    List<String> locations;
    String location;
    BigDecimal price;
    BigDecimal originalPrice;
    String currency;
    List<String> included;
    List<String> excluded;
    List<String> highlights;
    List<Map<String, Object>> itinerary;
    Boolean allowParent;
    Boolean allowTeacher;
    Boolean allowManager;
    Boolean individual;
    Instant createdAt;
    Instant updatedAt;
}


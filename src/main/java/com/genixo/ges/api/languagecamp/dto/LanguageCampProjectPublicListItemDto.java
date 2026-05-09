package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.EProjectType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampProjectPublicListItemDto {
    UUID id;
    String title;
    EProjectType projectType;
    String smallBanner;
    String location;
    String duration;
    BigDecimal price;
    BigDecimal originalPrice;
    String currency;
    Boolean individual;
}


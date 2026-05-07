package com.genixo.ges.api.reports.dto;

import com.genixo.ges.legal.model.ConsentType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsentStatsItemDto {
    ConsentType type;
    long activeDocuments;
    long acceptances;
}


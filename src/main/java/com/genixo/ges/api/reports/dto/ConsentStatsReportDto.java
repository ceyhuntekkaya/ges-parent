package com.genixo.ges.api.reports.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConsentStatsReportDto {
    List<ConsentStatsItemDto> items;
}


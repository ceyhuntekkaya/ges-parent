package com.genixo.ges.api.reports.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApplicationStatusCountDto {
    ApplicationStatus status;
    long count;
}


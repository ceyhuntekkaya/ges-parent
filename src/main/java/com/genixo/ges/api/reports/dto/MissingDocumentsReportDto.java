package com.genixo.ges.api.reports.dto;

import com.genixo.ges.docreq.model.DocumentRequirementScope;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MissingDocumentsReportDto {
    DocumentRequirementScope scope;
    UUID applicationId;
    List<String> missingRequiredKeys;
}


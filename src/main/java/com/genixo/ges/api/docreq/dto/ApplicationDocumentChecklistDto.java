package com.genixo.ges.api.docreq.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApplicationDocumentChecklistDto {
    List<RequirementChecklistItemDto> items;
    List<String> missingRequiredKeys;
}


package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.docreq.model.DocumentRequirementScope;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentRequirementDto {
    UUID id;
    DocumentRequirementScope scope;
    String category;
    String key;
    boolean required;
    String allowedContentTypes;
    long maxSizeBytes;
    String title;
    String description;
    boolean active;
}


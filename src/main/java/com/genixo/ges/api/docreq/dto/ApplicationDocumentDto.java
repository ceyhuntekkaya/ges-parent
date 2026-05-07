package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.docreq.model.ApplicationDocumentStatus;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApplicationDocumentDto {
    UUID id;
    DocumentRequirementScope scope;
    UUID applicationId;
    UUID relatedEntityId;
    UUID requirementId;
    String requirementKey;
    ApplicationDocumentStatus status;
    Instant uploadedAt;
    UUID uploadedByUserId;
    String reviewNote;
    StoredFileDto file;
}


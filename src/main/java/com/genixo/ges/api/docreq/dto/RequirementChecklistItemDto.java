package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.docreq.model.ApplicationDocumentStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequirementChecklistItemDto {
    DocumentRequirementDto requirement;
    boolean uploaded;
    UUID applicationDocumentId;
    ApplicationDocumentStatus status;
    String reviewNote;
    StoredFileDto file;
    String downloadUrl;
}


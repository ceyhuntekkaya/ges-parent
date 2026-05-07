package com.genixo.ges.api.docreq.dto;

import com.genixo.ges.docreq.model.ApplicationDocumentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ApplicationDocumentReviewRequestDto {
    @NotNull
    ApplicationDocumentStatus status; // APPROVED / REJECTED

    String reviewNote;
}


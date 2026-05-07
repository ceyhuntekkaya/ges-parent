package com.genixo.ges.api.storage.dto;

import com.genixo.ges.storage.model.StoredFilePurpose;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StoredFileDto {
    UUID id;
    StoredFilePurpose purpose;
    String originalFilename;
    String contentType;
    long sizeBytes;
    String sha256;
    UUID uploadedByUserId;
    Instant createdAt;
}


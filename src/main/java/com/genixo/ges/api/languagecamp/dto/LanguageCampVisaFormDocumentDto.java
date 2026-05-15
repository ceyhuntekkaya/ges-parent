package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.api.storage.dto.StoredFileDto;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LanguageCampVisaFormDocumentDto {
    UUID id;
    StoredFileDto file;
}

package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDocumentDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.model.LanguageCampVisaFormDocument;
import com.genixo.ges.storage.model.StoredFile;
import java.util.List;

final class LanguageCampVisaFormDtoMapper {

    private LanguageCampVisaFormDtoMapper() {}

    static LanguageCampVisaFormDto toDto(LanguageCampVisaForm f) {
        if (f == null) {
            return null;
        }

        List<LanguageCampVisaFormDocumentDto> documents = f.getDocuments() == null
            ? List.of()
            : f.getDocuments().stream()
                .map(LanguageCampVisaFormDtoMapper::toDocumentDto)
                .toList();

        return LanguageCampVisaFormDto.builder()
            .id(f.getId())
            .applicationId(f.getApplication() == null ? null : f.getApplication().getId())
            .passportNumber(f.getPassportNumber())
            .passportValidUntil(f.getPassportValidUntil())
            .passportType(f.getPassportType())
            .visaValidFrom(f.getVisaValidFrom())
            .visaValidUntil(f.getVisaValidUntil())
            .visaIssuingCountry(f.getVisaIssuingCountry())
            .visaType(f.getVisaType())
            .documents(documents)
            .createdAt(f.getCreatedAt())
            .updatedAt(f.getUpdatedAt())
            .build();
    }

    private static LanguageCampVisaFormDocumentDto toDocumentDto(LanguageCampVisaFormDocument doc) {
        if (doc == null) {
            return null;
        }
        return LanguageCampVisaFormDocumentDto.builder()
            .id(doc.getId())
            .file(toStoredFileDto(doc.getStoredFile()))
            .build();
    }

    private static StoredFileDto toStoredFileDto(StoredFile sf) {
        if (sf == null) {
            return null;
        }
        return StoredFileDto.builder()
            .id(sf.getId())
            .purpose(sf.getPurpose())
            .originalFilename(sf.getOriginalFilename())
            .contentType(sf.getContentType())
            .sizeBytes(sf.getSizeBytes())
            .sha256(sf.getSha256())
            .uploadedByUserId(sf.getUploadedBy() == null ? null : sf.getUploadedBy().getId())
            .createdAt(sf.getCreatedAt())
            .build();
    }
}

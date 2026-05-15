package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDocumentAttachRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormUpsertRequestDto;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.model.LanguageCampVisaFormDocument;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormDocumentRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormRepository;
import com.genixo.ges.languagecamp.service.LanguageCampVisaFormService;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.repo.StoredFileRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/language-camp-visa-forms")
public class LanguageCampVisaFormPortalController {

    private final LanguageCampVisaFormRepository forms;
    private final LanguageCampVisaFormDocumentRepository formDocuments;
    private final LanguageCampApplicationRepository applications;
    private final StoredFileRepository storedFiles;
    private final LanguageCampVisaFormService visaFormService;

    public LanguageCampVisaFormPortalController(
        LanguageCampVisaFormRepository forms,
        LanguageCampVisaFormDocumentRepository formDocuments,
        LanguageCampApplicationRepository applications,
        StoredFileRepository storedFiles,
        LanguageCampVisaFormService visaFormService
    ) {
        this.forms = forms;
        this.formDocuments = formDocuments;
        this.applications = applications;
        this.storedFiles = storedFiles;
        this.visaFormService = visaFormService;
    }

    @PostMapping("/ensure")
    @Transactional
    @Operation(operationId = "portalLanguageCampVisaFormsEnsure")
    public ResponseEntity<LanguageCampVisaFormDto> ensure(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam UUID applicationId
    ) {
        var app = applications.findByIdAndApplicant_Id(applicationId, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        LanguageCampVisaForm f = visaFormService.ensureForApplication(app);
        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(
            forms.findByApplicationIdWithDocuments(applicationId).orElse(f)
        ));
    }

    @GetMapping
    @Operation(operationId = "portalLanguageCampVisaFormsListByApplication")
    public ResponseEntity<PageDto<LanguageCampVisaFormDto>> listByApplication(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam UUID applicationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        applications.findByIdAndApplicant_Id(applicationId, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = forms.findAllByApplication_Id(applicationId, pageable);
        var items = p.getContent().stream()
            .map(f -> forms.findByIdWithDocuments(f.getId()).orElse(f))
            .map(LanguageCampVisaFormDtoMapper::toDto)
            .toList();

        return ResponseEntity.ok(PageDto.<LanguageCampVisaFormDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "portalLanguageCampVisaFormsGet")
    public ResponseEntity<LanguageCampVisaFormDto> get(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampVisaForm f = forms.findByIdWithDocuments(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        assertOwner(f, principal.getId());
        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(f));
    }

    @PatchMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalLanguageCampVisaFormsUpdate")
    public ResponseEntity<LanguageCampVisaFormDto> update(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampVisaFormUpsertRequestDto req
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        assertOwner(f, principal.getId());
        apply(f, req);
        forms.save(f);
        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(
            forms.findByIdWithDocuments(f.getId()).orElse(f)
        ));
    }

    @PostMapping("/{id}/documents")
    @Transactional
    @Operation(operationId = "portalLanguageCampVisaFormsDocumentsAdd")
    public ResponseEntity<LanguageCampVisaFormDto> addDocument(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampVisaFormDocumentAttachRequestDto req
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        assertOwner(f, principal.getId());

        StoredFile sf = storedFiles.findById(req.getFileId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid fileId"));
        if (sf.getUploadedBy() == null || !principal.getId().equals(sf.getUploadedBy().getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        boolean alreadyAttached = f.getDocuments() != null && f.getDocuments().stream()
            .anyMatch(d -> d.getStoredFile() != null && req.getFileId().equals(d.getStoredFile().getId()));
        if (alreadyAttached) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "File already attached");
        }

        LanguageCampVisaFormDocument doc = new LanguageCampVisaFormDocument();
        doc.setVisaForm(f);
        doc.setStoredFile(sf);

        List<LanguageCampVisaFormDocument> list = f.getDocuments();
        if (list == null) {
            list = new ArrayList<>();
            f.setDocuments(list);
        }
        list.add(doc);
        forms.save(f);

        return ResponseEntity.status(HttpStatus.CREATED).body(LanguageCampVisaFormDtoMapper.toDto(
            forms.findByIdWithDocuments(f.getId()).orElse(f)
        ));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "portalLanguageCampVisaFormsDocumentsDelete")
    public ResponseEntity<LanguageCampVisaFormDto> deleteDocument(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID documentId
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        assertOwner(f, principal.getId());

        LanguageCampVisaFormDocument doc = formDocuments.findByIdAndVisaForm_Id(documentId, id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));

        if (f.getDocuments() != null) {
            f.getDocuments().removeIf(d -> d.getId().equals(doc.getId()));
        }
        forms.save(f);

        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(
            forms.findByIdWithDocuments(f.getId()).orElse(f)
        ));
    }

    private void assertOwner(LanguageCampVisaForm f, UUID currentUserId) {
        if (f.getApplication() == null
            || f.getApplication().getApplicant() == null
            || !currentUserId.equals(f.getApplication().getApplicant().getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private void apply(LanguageCampVisaForm f, LanguageCampVisaFormUpsertRequestDto req) {
        f.setPassportNumber(trimToNull(req.getPassportNumber()));
        f.setPassportValidUntil(req.getPassportValidUntil());
        f.setPassportType(req.getPassportType());
        f.setVisaValidFrom(req.getVisaValidFrom());
        f.setVisaValidUntil(req.getVisaValidUntil());
        f.setVisaIssuingCountry(trimToNull(req.getVisaIssuingCountry()));
        f.setVisaType(trimToNull(req.getVisaType()));
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

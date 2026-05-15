package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDocumentAttachRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.model.LanguageCampVisaFormDocument;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormDocumentRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/language-camp-visa-forms")
public class LanguageCampVisaFormAdminController {

    private final LanguageCampVisaFormRepository forms;
    private final LanguageCampVisaFormDocumentRepository formDocuments;
    private final StoredFileRepository storedFiles;

    public LanguageCampVisaFormAdminController(
        LanguageCampVisaFormRepository forms,
        LanguageCampVisaFormDocumentRepository formDocuments,
        StoredFileRepository storedFiles
    ) {
        this.forms = forms;
        this.formDocuments = formDocuments;
        this.storedFiles = storedFiles;
    }

    @GetMapping
    @Operation(operationId = "adminLanguageCampVisaFormsListByApplication")
    public ResponseEntity<PageDto<LanguageCampVisaFormDto>> listByApplication(
        @RequestParam UUID applicationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
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
    @Operation(operationId = "adminLanguageCampVisaFormsGet")
    public ResponseEntity<LanguageCampVisaFormDto> get(@PathVariable UUID id) {
        LanguageCampVisaForm f = forms.findByIdWithDocuments(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));
        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(f));
    }

    @PostMapping("/{id}/documents")
    @Transactional
    @Operation(operationId = "adminLanguageCampVisaFormsDocumentsAdd")
    public ResponseEntity<LanguageCampVisaFormDto> addDocument(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampVisaFormDocumentAttachRequestDto req
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        StoredFile sf = storedFiles.findById(req.getFileId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid fileId"));

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
    @Operation(operationId = "adminLanguageCampVisaFormsDocumentsDelete")
    public ResponseEntity<LanguageCampVisaFormDto> deleteDocument(
        @PathVariable UUID id,
        @PathVariable UUID documentId
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        formDocuments.findByIdAndVisaForm_Id(documentId, id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));

        if (f.getDocuments() != null) {
            f.getDocuments().removeIf(d -> d.getId().equals(documentId));
        }
        forms.save(f);

        return ResponseEntity.ok(LanguageCampVisaFormDtoMapper.toDto(
            forms.findByIdWithDocuments(f.getId()).orElse(f)
        ));
    }
}

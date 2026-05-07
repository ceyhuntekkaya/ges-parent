package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.docreq.dto.ApplicationDocumentAttachRequestDto;
import com.genixo.ges.api.docreq.dto.ApplicationDocumentDto;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.repo.ApplicationDocumentRepository;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.storage.FileStorageService;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.repo.StoredFileRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/application-documents")
public class PortalApplicationDocumentController {

    private final ApplicationDocumentRepository docs;
    private final DocumentRequirementRepository requirements;
    private final StoredFileRepository storedFiles;
    private final PortalDocOwnershipService ownership;
    private final FileStorageService storage;
    private final RequirementFileValidator validator;

    public PortalApplicationDocumentController(
        ApplicationDocumentRepository docs,
        DocumentRequirementRepository requirements,
        StoredFileRepository storedFiles,
        PortalDocOwnershipService ownership,
        FileStorageService storage,
        RequirementFileValidator validator
    ) {
        this.docs = docs;
        this.requirements = requirements;
        this.storedFiles = storedFiles;
        this.ownership = ownership;
        this.storage = storage;
        this.validator = validator;
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "portalApplicationDocumentsAttach")
    public ResponseEntity<ApplicationDocumentDto> attach(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody ApplicationDocumentAttachRequestDto req
    ) {
        ownership.assertOwner(req.getScope(), req.getApplicationId(), principal.getId());

        StoredFile file = storedFiles.findById(req.getFileId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid fileId"));

        UUID uploadedBy = file.getUploadedBy() == null ? null : file.getUploadedBy().getId();
        if (uploadedBy == null || !uploadedBy.equals(principal.getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        DocumentRequirement requirement = resolveRequirement(req);
        validator.validate(requirement, file);

        ApplicationDocument doc = new ApplicationDocument();
        doc.setScope(req.getScope());
        doc.setApplicationId(req.getApplicationId());
        doc.setRelatedEntityId(req.getRelatedEntityId());
        doc.setRequirement(requirement);
        doc.setRequirementKey(req.getRequirementKey());
        doc.setFile(file);
        doc.setUploadedAt(Instant.now());
        // uploadedBy on doc (denormalized)
        doc.setUploadedBy(file.getUploadedBy());
        docs.save(doc);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(doc));
    }

    @GetMapping("/by-application")
    @Operation(operationId = "portalApplicationDocumentsListByApplication")
    public ResponseEntity<PageDto<ApplicationDocumentDto>> listByApplication(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam UUID applicationId,
        @RequestParam com.genixo.ges.docreq.model.DocumentRequirementScope scope,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        ownership.assertOwner(scope, applicationId, principal.getId());

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        var p = docs.findByScopeAndApplicationId(scope, applicationId, pageable);

        var items = p.getContent().stream().map(this::toDto).toList();

        return ResponseEntity.ok(PageDto.<ApplicationDocumentDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "portalApplicationDocumentsGet")
    public ResponseEntity<ApplicationDocumentDto> get(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
        ownership.assertOwner(doc.getScope(), doc.getApplicationId(), principal.getId());
        return ResponseEntity.ok(toDto(doc));
    }

    @GetMapping("/{id}/file")
    @Operation(operationId = "portalApplicationDocumentsDownloadFile")
    public ResponseEntity<Resource> downloadFile(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
        ownership.assertOwner(doc.getScope(), doc.getApplicationId(), principal.getId());

        StoredFile sf = doc.getFile();
        var path = storage.resolvePath(sf);
        if (!java.nio.file.Files.exists(path)) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "File not found on disk");
        }

        Resource res = new FileSystemResource(path);
        String ct = sf.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : sf.getContentType();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sf.getOriginalFilename().replace("\"", "") + "\"")
            .contentType(MediaType.parseMediaType(ct))
            .contentLength(sf.getSizeBytes())
            .body(res);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalApplicationDocumentsDelete")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
        ownership.assertOwner(doc.getScope(), doc.getApplicationId(), principal.getId());

        // NOTE: deleting the document record does not delete the underlying file.
        docs.delete(doc);
        return ResponseEntity.noContent().build();
    }

    private ApplicationDocumentDto toDto(ApplicationDocument d) {
        StoredFile f = d.getFile();
        StoredFileDto fileDto = StoredFileDto.builder()
            .id(f.getId())
            .purpose(f.getPurpose())
            .originalFilename(f.getOriginalFilename())
            .contentType(f.getContentType())
            .sizeBytes(f.getSizeBytes())
            .sha256(f.getSha256())
            .uploadedByUserId(f.getUploadedBy() == null ? null : f.getUploadedBy().getId())
            .createdAt(f.getCreatedAt())
            .build();

        return ApplicationDocumentDto.builder()
            .id(d.getId())
            .scope(d.getScope())
            .applicationId(d.getApplicationId())
            .relatedEntityId(d.getRelatedEntityId())
            .requirementId(d.getRequirement() == null ? null : d.getRequirement().getId())
            .requirementKey(d.getRequirementKey())
            .status(d.getStatus())
            .uploadedAt(d.getUploadedAt())
            .uploadedByUserId(d.getUploadedBy() == null ? null : d.getUploadedBy().getId())
            .reviewNote(d.getReviewNote())
            .file(fileDto)
            .build();
    }

    private DocumentRequirement resolveRequirement(ApplicationDocumentAttachRequestDto req) {
        if (req.getRequirementId() != null) {
            return requirements.findById(req.getRequirementId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid requirementId"));
        }
        return requirements.findByScopeAndKey(req.getScope(), req.getRequirementKey())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid requirementKey for scope"));
    }
}


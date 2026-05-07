package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.docreq.dto.ApplicationDocumentDto;
import com.genixo.ges.api.docreq.dto.ApplicationDocumentReviewRequestDto;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.repo.ApplicationDocumentRepository;
import com.genixo.ges.storage.FileStorageService;
import com.genixo.ges.storage.model.StoredFile;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/application-documents")
public class AdminApplicationDocumentController {

    private final ApplicationDocumentRepository docs;
    private final FileStorageService storage;

    public AdminApplicationDocumentController(ApplicationDocumentRepository docs, FileStorageService storage) {
        this.docs = docs;
        this.storage = storage;
    }

    @GetMapping
    @Operation(operationId = "adminApplicationDocumentsList")
    public ResponseEntity<PageDto<ApplicationDocumentDto>> list(
        @RequestParam com.genixo.ges.docreq.model.DocumentRequirementScope scope,
        @RequestParam UUID applicationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        var p = docs.findByScopeAndApplicationId(scope, applicationId, pageable);
        return ResponseEntity.ok(PageDto.<ApplicationDocumentDto>builder()
            .items(p.getContent().stream().map(this::toDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "adminApplicationDocumentsGet")
    public ResponseEntity<ApplicationDocumentDto> get(@PathVariable UUID id) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
        return ResponseEntity.ok(toDto(doc));
    }

    @GetMapping("/{id}/file")
    @Operation(operationId = "adminApplicationDocumentsDownloadFile")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
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

    @PatchMapping("/{id}/review")
    @Transactional
    @Operation(operationId = "adminApplicationDocumentsReview")
    public ResponseEntity<ApplicationDocumentDto> review(@PathVariable UUID id, @Valid @RequestBody ApplicationDocumentReviewRequestDto req) {
        ApplicationDocument doc = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Document not found"));
        if (req.getStatus() == com.genixo.ges.docreq.model.ApplicationDocumentStatus.UPLOADED) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid review status");
        }
        doc.setStatus(req.getStatus());
        doc.setReviewNote(req.getReviewNote());
        docs.save(doc);
        return ResponseEntity.ok(toDto(doc));
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
}


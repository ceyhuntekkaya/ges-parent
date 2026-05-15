package com.genixo.ges.api.storage;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.storage.FileStorageService;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.model.StoredFilePurpose;
import com.genixo.ges.storage.repo.StoredFileRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.file.Files;
import java.util.Locale;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/admin/files")
public class AdminFileController {

    private final FileStorageService storage;
    private final StoredFileRepository storedFiles;
    private final UserAccountRepository users;

    public AdminFileController(FileStorageService storage, StoredFileRepository storedFiles, UserAccountRepository users) {
        this.storage = storage;
        this.storedFiles = storedFiles;
        this.users = users;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "adminFilesUpload")
    public ResponseEntity<StoredFileDto> upload(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam("file") MultipartFile file,
        @RequestParam(name = "purpose", defaultValue = "OTHER") StoredFilePurpose purpose
    ) {
        UserAccount uploader = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        StoredFile sf = storage.store(file, purpose, uploader);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUploadDto(sf));
    }

    @GetMapping
    @Operation(operationId = "adminFilesList")
    public ResponseEntity<PageDto<com.genixo.ges.api.storage.dto.StoredFileDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) StoredFilePurpose purpose
    ) {
        int safeSize = Math.min(100, Math.max(1, size));
        var pageable = PageRequest.of(Math.max(0, page), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        String query = q == null ? null : q.trim();
        var p = query == null || query.isEmpty()
            ? (purpose == null ? storedFiles.findAll(pageable) : storedFiles.findByPurpose(purpose, pageable))
            : (purpose == null
                ? storedFiles.findByOriginalFilenameContainingIgnoreCase(query.toLowerCase(Locale.ROOT), pageable)
                : storedFiles.findByPurposeAndOriginalFilenameContainingIgnoreCase(purpose, query.toLowerCase(Locale.ROOT), pageable));

        var items = p.getContent().stream().map(this::toDto).toList();
        return ResponseEntity.ok(PageDto.<com.genixo.ges.api.storage.dto.StoredFileDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}/download")
    @Operation(operationId = "adminFilesDownload")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        StoredFile sf = storedFiles.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "File not found"));

        var path = storage.resolvePath(sf);
        if (!Files.exists(path)) {
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

    private StoredFileDto toDto(StoredFile sf) {
        return toUploadDto(sf);
    }

    private StoredFileDto toUploadDto(StoredFile sf) {
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


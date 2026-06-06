package com.genixo.ges.api.storage;

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
import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
@RequestMapping("/v1/portal/files")
public class PortalFileController {

    private final FileStorageService storage;
    private final StoredFileRepository storedFiles;
    private final UserAccountRepository users;

    public PortalFileController(FileStorageService storage, StoredFileRepository storedFiles, UserAccountRepository users) {
        this.storage = storage;
        this.storedFiles = storedFiles;
        this.users = users;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "portalFilesUpload")
    public ResponseEntity<StoredFileDto> upload(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam("file") MultipartFile file,
        @RequestParam(name = "purpose", defaultValue = "OTHER") StoredFilePurpose purpose
    ) {
        UserAccount uploader = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        StoredFile sf = storage.store(file, purpose, uploader);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(sf));
    }

    @GetMapping("/{id}/download")
    @Operation(operationId = "portalFilesDownload")
    public ResponseEntity<Resource> download(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        HttpServletRequest req
    ) {
        StoredFile sf = storedFiles.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "File not found"));

        // owner check (portal). admin downloads should go through /admin endpoint later.
        UUID uploadedById = sf.getUploadedBy() == null ? null : sf.getUploadedBy().getId();
        if (uploadedById == null || !uploadedById.equals(principal.getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        var path = storage.resolvePath(sf);
        if (!Files.exists(path)) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "File not found on disk");
        }

        Resource res = new FileSystemResource(path);
        String ct = sf.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : sf.getContentType();

        // inline so images/videos/PDFs can render in embedded previews.
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sf.getOriginalFilename().replace("\"", "") + "\"")
            .contentType(MediaType.parseMediaType(ct))
            .contentLength(sf.getSizeBytes())
            .body(res);
    }

    private StoredFileDto toDto(StoredFile sf) {
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


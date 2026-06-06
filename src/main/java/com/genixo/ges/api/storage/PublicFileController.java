package com.genixo.ges.api.storage;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.storage.FileStorageService;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.model.StoredFilePurpose;
import com.genixo.ges.storage.repo.StoredFileRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.file.Files;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public/files")
public class PublicFileController {

    private final FileStorageService storage;
    private final StoredFileRepository storedFiles;

    public PublicFileController(FileStorageService storage, StoredFileRepository storedFiles) {
        this.storage = storage;
        this.storedFiles = storedFiles;
    }

    @GetMapping("/{id}/download")
    @Operation(operationId = "publicFilesDownload")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        StoredFile sf = storedFiles.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "File not found"));

        // Backwards-compat: earlier uploads used OTHER by default.
        // We only allow a narrow set of purposes to be public.
        if (sf.getPurpose() != StoredFilePurpose.HOME_PAGE_MEDIA
            && sf.getPurpose() != StoredFilePurpose.PROJECT_MEDIA
            && sf.getPurpose() != StoredFilePurpose.OTHER) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "File not found");
        }

        var path = storage.resolvePath(sf);
        if (!Files.exists(path)) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "File not found on disk");
        }

        Resource res = new FileSystemResource(path);
        String ct = sf.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : sf.getContentType();

        // Use inline so images/videos can render in <img>/<video> tags.
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sf.getOriginalFilename().replace("\"", "") + "\"")
            .contentType(MediaType.parseMediaType(ct))
            .contentLength(sf.getSizeBytes())
            .body(res);
    }
}


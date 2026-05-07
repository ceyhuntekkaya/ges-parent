package com.genixo.ges.storage;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.model.StoredFilePurpose;
import com.genixo.ges.storage.repo.StoredFileRepository;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final StorageProperties props;
    private final StoredFileRepository storedFiles;

    public FileStorageService(StorageProperties props, StoredFileRepository storedFiles) {
        this.props = props;
        this.storedFiles = storedFiles;
    }

    @Transactional
    public StoredFile store(MultipartFile file, StoredFilePurpose purpose, UserAccount uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "File is required");
        }
        if (!StringUtils.hasText(props.getBase())) {
            throw new ApiProblemException(HttpStatus.INTERNAL_SERVER_ERROR, "Storage base path not configured");
        }

        String original = Path.of(OptionalSafe.filename(file.getOriginalFilename())).getFileName().toString();
        String contentType = OptionalSafe.contentType(file.getContentType());
        long size = file.getSize();

        UUID id = UUID.randomUUID(); // use for filesystem key; DB will have its own id
        String ext = guessExtension(original, contentType);
        String key = buildStorageKey(purpose, id, ext);

        Path base = Path.of(props.getBase());
        Path target = base.resolve(key);
        try {
            Files.createDirectories(target.getParent());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) >= 0) {
                    if (r == 0) continue;
                    md.update(buf, 0, r);
                }
            }
            String sha256 = HexFormat.of().formatHex(md.digest());

            // write file (second pass). For large files we could stream once to temp+hash; keep simple.
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            StoredFile sf = new StoredFile();
            sf.setPurpose(purpose == null ? StoredFilePurpose.OTHER : purpose);
            sf.setStorageKey(key);
            sf.setOriginalFilename(original);
            sf.setContentType(contentType);
            sf.setSizeBytes(size);
            sf.setSha256(sha256);
            sf.setUploadedBy(uploadedBy);
            return storedFiles.save(sf);
        } catch (Exception e) {
            throw new ApiProblemException(HttpStatus.INTERNAL_SERVER_ERROR, "File storage failed");
        }
    }

    public Path resolvePath(StoredFile storedFile) {
        Path base = Path.of(props.getBase());
        return base.resolve(storedFile.getStorageKey());
    }

    private static String buildStorageKey(StoredFilePurpose purpose, UUID random, String ext) {
        String p = (purpose == null ? StoredFilePurpose.OTHER : purpose).name().toLowerCase();
        String date = java.time.LocalDate.now().toString();
        return p + "/" + date + "/" + random + (ext.isBlank() ? "" : ("." + ext));
    }

    private static String guessExtension(String original, String contentType) {
        String name = original == null ? "" : original;
        int idx = name.lastIndexOf('.');
        if (idx > 0 && idx < name.length() - 1) {
            return name.substring(idx + 1).toLowerCase();
        }
        if ("application/pdf".equalsIgnoreCase(contentType)) return "pdf";
        if ("image/jpeg".equalsIgnoreCase(contentType)) return "jpg";
        if ("image/png".equalsIgnoreCase(contentType)) return "png";
        return "";
    }

    // small helpers to avoid null surprises
    private static class OptionalSafe {
        static String filename(String v) {
            return StringUtils.hasText(v) ? v : "file";
        }

        static String contentType(String v) {
            return StringUtils.hasText(v) ? v : "application/octet-stream";
        }
    }
}


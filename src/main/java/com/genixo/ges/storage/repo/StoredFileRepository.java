package com.genixo.ges.storage.repo;

import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.model.StoredFilePurpose;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Page<StoredFile> findByPurpose(StoredFilePurpose purpose, Pageable pageable);

    Page<StoredFile> findByOriginalFilenameContainingIgnoreCase(String q, Pageable pageable);

    Page<StoredFile> findByPurposeAndOriginalFilenameContainingIgnoreCase(StoredFilePurpose purpose, String q, Pageable pageable);
}


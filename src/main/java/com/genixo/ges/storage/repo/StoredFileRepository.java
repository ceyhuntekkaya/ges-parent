package com.genixo.ges.storage.repo;

import com.genixo.ges.storage.model.StoredFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {}


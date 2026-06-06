package com.genixo.ges.languagecamp.repo;

import java.time.Instant;
import java.util.UUID;

public interface LanguageCampApplicationGroupKeyRow {
    UUID getApplicantUserId();

    UUID getLanguageCampProjectId();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    Long getParticipantCount();
}

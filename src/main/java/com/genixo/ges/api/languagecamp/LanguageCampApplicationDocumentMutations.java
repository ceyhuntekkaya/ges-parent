package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDocumentUpsertRequestDto;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationDocument;
import java.time.Instant;

public final class LanguageCampApplicationDocumentMutations {

    private LanguageCampApplicationDocumentMutations() {}

    public static String normalizeDocumentUrl(String url) {
        if (url == null) {
            return null;
        }
        String t = url.trim();
        return t.isEmpty() ? null : t;
    }

    public static void applyForCreate(LanguageCampApplicationDocument d, LanguageCampApplicationDocumentUpsertRequestDto req) {
        d.setRequired(req.getRequired() != null && req.getRequired());
        d.setDocumentName(req.getDocumentName());
        d.setDocumentDescription(req.getDocumentDescription());
        String url = normalizeDocumentUrl(req.getDocumentUrl());
        d.setDocumentUrl(url);
        d.setUploadedAt(url != null ? Instant.now() : null);
    }

    public static void applyForUpdate(LanguageCampApplicationDocument d, LanguageCampApplicationDocumentUpsertRequestDto req) {
        d.setRequired(req.getRequired() != null && req.getRequired());
        d.setDocumentName(req.getDocumentName());
        d.setDocumentDescription(req.getDocumentDescription());
        String newUrl = normalizeDocumentUrl(req.getDocumentUrl());
        String oldUrl = d.getDocumentUrl();
        d.setDocumentUrl(newUrl);
        if (newUrl == null) {
            d.setUploadedAt(null);
        } else if (oldUrl == null || !newUrl.equals(oldUrl)) {
            d.setUploadedAt(Instant.now());
        }
    }
}

package com.genixo.ges.api.university;

import com.genixo.ges.api.university.dto.UniversityApplicationDocumentUpsertRequestDto;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import java.time.Instant;

public final class UniversityApplicationDocumentMutations {

    private UniversityApplicationDocumentMutations() {}

    public static String normalizeDocumentUrl(String url) {
        if (url == null) {
            return null;
        }
        String t = url.trim();
        return t.isEmpty() ? null : t;
    }

    public static void applyForCreate(UniversityApplicationDocument d, UniversityApplicationDocumentUpsertRequestDto req) {
        d.setRequired(req.getRequired() != null && req.getRequired());
        d.setDocumentName(req.getDocumentName());
        d.setDocumentDescription(req.getDocumentDescription());
        String url = normalizeDocumentUrl(req.getDocumentUrl());
        d.setDocumentUrl(url);
        d.setUploadedAt(url != null ? Instant.now() : null);
    }

    public static void applyForUpdate(UniversityApplicationDocument d, UniversityApplicationDocumentUpsertRequestDto req) {
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

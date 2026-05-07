package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.storage.model.StoredFile;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RequirementFileValidator {

    public void validate(DocumentRequirement req, StoredFile file) {
        if (req == null) throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Requirement is required");
        if (file == null) throw new ApiProblemException(HttpStatus.BAD_REQUEST, "File is required");

        if (!req.isActive()) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Document requirement is not active");
        }

        long max = req.getMaxSizeBytes();
        if (max > 0 && file.getSizeBytes() > max) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "File size exceeds maxSizeBytes");
        }

        String allowed = req.getAllowedContentTypes();
        if (StringUtils.hasText(allowed)) {
            String ct = StringUtils.hasText(file.getContentType()) ? file.getContentType() : "application/octet-stream";
            if (!isAllowed(ct, allowed)) {
                throw new ApiProblemException(HttpStatus.BAD_REQUEST, "File contentType is not allowed");
            }
        }
    }

    private boolean isAllowed(String contentType, String allowedCsv) {
        String ct = contentType.toLowerCase(Locale.ROOT).trim();
        Set<String> allowed = Arrays.stream(allowedCsv.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

        if (allowed.contains(ct)) return true;

        int slash = ct.indexOf('/');
        if (slash > 0) {
            String wildcard = ct.substring(0, slash) + "/*";
            return allowed.contains(wildcard);
        }
        return false;
    }
}


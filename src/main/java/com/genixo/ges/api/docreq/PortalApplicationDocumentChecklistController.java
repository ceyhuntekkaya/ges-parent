package com.genixo.ges.api.docreq;

import com.genixo.ges.api.docreq.dto.ApplicationDocumentChecklistDto;
import com.genixo.ges.api.docreq.dto.DocumentRequirementDto;
import com.genixo.ges.api.docreq.dto.RequirementChecklistItemDto;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.ApplicationDocumentRepository;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.storage.model.StoredFile;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/portal/application-document-checklist")
public class PortalApplicationDocumentChecklistController {

    private final DocumentRequirementRepository requirements;
    private final ApplicationDocumentRepository docs;
    private final PortalDocOwnershipService ownership;

    public PortalApplicationDocumentChecklistController(
        DocumentRequirementRepository requirements,
        ApplicationDocumentRepository docs,
        PortalDocOwnershipService ownership
    ) {
        this.requirements = requirements;
        this.docs = docs;
        this.ownership = ownership;
    }

    @GetMapping
    public ResponseEntity<ApplicationDocumentChecklistDto> getChecklist(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam DocumentRequirementScope scope,
        @RequestParam UUID applicationId,
        @RequestParam(required = false) String category
    ) {
        ownership.assertOwner(scope, applicationId, principal.getId());

        var reqPageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.ASC, "key"));
        List<DocumentRequirement> reqs = requirements.findByScopeAndActiveTrue(scope, reqPageable)
            .getContent()
            .stream()
            .filter(r -> category == null || category.isBlank() || (r.getCategory() != null && r.getCategory().equalsIgnoreCase(category)))
            .toList();

        var docPageable = PageRequest.of(0, 500, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        List<ApplicationDocument> existingDocs = docs.findByScopeAndApplicationId(scope, applicationId, docPageable).getContent();

        Map<String, ApplicationDocument> latestByKey = new HashMap<>();
        for (ApplicationDocument d : existingDocs) {
            String k = d.getRequirementKey();
            if (k == null || k.isBlank()) continue;
            latestByKey.merge(k, d, (a, b) -> {
                if (a.getUploadedAt() == null) return b;
                if (b.getUploadedAt() == null) return a;
                return a.getUploadedAt().isAfter(b.getUploadedAt()) ? a : b;
            });
        }

        String base = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        List<RequirementChecklistItemDto> items = reqs.stream()
            .map(r -> {
                DocumentRequirementDto rd = toRequirementDto(r);
                ApplicationDocument d = latestByKey.get(r.getKey());
                if (d == null) {
                    return RequirementChecklistItemDto.builder()
                        .requirement(rd)
                        .uploaded(false)
                        .build();
                }

                StoredFile f = d.getFile();
                StoredFileDto fd = toFileDto(f);
                String downloadUrl = base + "/api/v1/portal/application-documents/" + d.getId() + "/file";

                return RequirementChecklistItemDto.builder()
                    .requirement(rd)
                    .uploaded(true)
                    .applicationDocumentId(d.getId())
                    .status(d.getStatus())
                    .reviewNote(d.getReviewNote())
                    .file(fd)
                    .downloadUrl(downloadUrl)
                    .build();
            })
            .sorted(Comparator.comparing(i -> i.getRequirement().getKey()))
            .toList();

        List<String> missing = reqs.stream()
            .filter(DocumentRequirement::isRequired)
            .map(DocumentRequirement::getKey)
            .filter(k -> !latestByKey.containsKey(k))
            .sorted()
            .toList();

        return ResponseEntity.ok(ApplicationDocumentChecklistDto.builder()
            .items(items)
            .missingRequiredKeys(missing)
            .build());
    }

    private DocumentRequirementDto toRequirementDto(DocumentRequirement r) {
        return DocumentRequirementDto.builder()
            .id(r.getId())
            .scope(r.getScope())
            .category(r.getCategory())
            .key(r.getKey())
            .required(r.isRequired())
            .allowedContentTypes(r.getAllowedContentTypes())
            .maxSizeBytes(r.getMaxSizeBytes())
            .title(r.getTitle())
            .description(r.getDescription())
            .active(r.isActive())
            .build();
    }

    private StoredFileDto toFileDto(StoredFile f) {
        return StoredFileDto.builder()
            .id(f.getId())
            .purpose(f.getPurpose())
            .originalFilename(f.getOriginalFilename())
            .contentType(f.getContentType())
            .sizeBytes(f.getSizeBytes())
            .sha256(f.getSha256())
            .uploadedByUserId(f.getUploadedBy() == null ? null : f.getUploadedBy().getId())
            .createdAt(f.getCreatedAt())
            .build();
    }
}


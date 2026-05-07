package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.docreq.dto.DocumentRequirementDto;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/document-requirements")
public class PortalDocumentRequirementController {

    private final DocumentRequirementRepository requirements;

    public PortalDocumentRequirementController(DocumentRequirementRepository requirements) {
        this.requirements = requirements;
    }

    @GetMapping
    @Operation(operationId = "portalDocumentRequirementsList")
    public ResponseEntity<PageDto<DocumentRequirementDto>> listActive(
        @RequestParam DocumentRequirementScope scope,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "key"));
        var p = requirements.findByScopeAndActiveTrue(scope, pageable);

        var items = p.getContent().stream()
            .filter(r -> category == null || category.isBlank() || (r.getCategory() != null && r.getCategory().equalsIgnoreCase(category)))
            .map(r -> DocumentRequirementDto.builder()
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
                .build()
            )
            .toList();

        return ResponseEntity.ok(PageDto.<DocumentRequirementDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }
}


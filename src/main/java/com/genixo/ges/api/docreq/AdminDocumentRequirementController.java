package com.genixo.ges.api.docreq;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.docreq.dto.DocumentRequirementDto;
import com.genixo.ges.api.docreq.dto.DocumentRequirementUpsertRequestDto;
import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/document-requirements")
public class AdminDocumentRequirementController {

    private final DocumentRequirementRepository requirements;

    public AdminDocumentRequirementController(DocumentRequirementRepository requirements) {
        this.requirements = requirements;
    }

    @GetMapping
    @Operation(operationId = "adminDocumentRequirementsList")
    public ResponseEntity<PageDto<DocumentRequirementDto>> list(
        @RequestParam DocumentRequirementScope scope,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "key"));
        var p = requirements.findAll(pageable);

        var items = p.getContent().stream()
            .filter(r -> r.getScope() == scope)
            .filter(r -> category == null || category.isBlank() || (r.getCategory() != null && r.getCategory().equalsIgnoreCase(category)))
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(PageDto.<DocumentRequirementDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "adminDocumentRequirementsGet")
    public ResponseEntity<DocumentRequirementDto> get(@PathVariable UUID id) {
        DocumentRequirement r = requirements.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Requirement not found"));
        return ResponseEntity.ok(toDto(r));
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "adminDocumentRequirementsCreate")
    public ResponseEntity<DocumentRequirementDto> create(@Valid @RequestBody DocumentRequirementUpsertRequestDto req) {
        requirements.findByScopeAndKey(req.getScope(), req.getKey()).ifPresent(x -> {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Requirement key already exists for scope");
        });
        DocumentRequirement r = new DocumentRequirement();
        apply(r, req);
        requirements.save(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(r));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminDocumentRequirementsUpdate")
    public ResponseEntity<DocumentRequirementDto> update(@PathVariable UUID id, @Valid @RequestBody DocumentRequirementUpsertRequestDto req) {
        DocumentRequirement r = requirements.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Requirement not found"));
        apply(r, req);
        requirements.save(r);
        return ResponseEntity.ok(toDto(r));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminDocumentRequirementsDelete")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!requirements.existsById(id)) throw new ApiProblemException(HttpStatus.NOT_FOUND, "Requirement not found");
        requirements.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(DocumentRequirement r, DocumentRequirementUpsertRequestDto req) {
        r.setScope(req.getScope());
        r.setCategory(req.getCategory());
        r.setKey(req.getKey().trim());
        r.setRequired(Boolean.TRUE.equals(req.getRequired()));
        r.setAllowedContentTypes(req.getAllowedContentTypes());
        if (req.getMaxSizeBytes() != null) r.setMaxSizeBytes(req.getMaxSizeBytes());
        r.setTitle(req.getTitle());
        r.setDescription(req.getDescription());
        if (req.getActive() != null) r.setActive(req.getActive());
    }

    private DocumentRequirementDto toDto(DocumentRequirement r) {
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
}


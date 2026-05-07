package com.genixo.ges.api.legal;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.legal.dto.ConsentDocumentDto;
import com.genixo.ges.api.legal.dto.ConsentDocumentUpsertRequestDto;
import com.genixo.ges.legal.model.ConsentDocument;
import com.genixo.ges.legal.model.ConsentType;
import com.genixo.ges.legal.repo.ConsentDocumentRepository;
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
@RequestMapping("/v1/admin/consent-documents")
public class AdminConsentDocumentController {

    private final ConsentDocumentRepository docs;

    public AdminConsentDocumentController(ConsentDocumentRepository docs) {
        this.docs = docs;
    }

    @GetMapping
    public ResponseEntity<PageDto<ConsentDocumentDto>> list(
        @RequestParam(required = false) ConsentType type,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = docs.findAll(pageable);
        var items = p.getContent().stream()
            .filter(d -> type == null || d.getType() == type)
            .filter(d -> active == null || d.isActive() == active)
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(PageDto.<ConsentDocumentDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsentDocumentDto> get(@PathVariable UUID id) {
        ConsentDocument d = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Consent document not found"));
        return ResponseEntity.ok(toDto(d));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ConsentDocumentDto> create(@Valid @RequestBody ConsentDocumentUpsertRequestDto req) {
        docs.findByTypeAndLanguageAndVersion(req.getType(), req.getLanguage(), req.getVersion()).ifPresent(x -> {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Consent document already exists (type+language+version)");
        });

        ConsentDocument d = new ConsentDocument();
        apply(d, req);
        docs.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(d));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ConsentDocumentDto> update(@PathVariable UUID id, @Valid @RequestBody ConsentDocumentUpsertRequestDto req) {
        ConsentDocument d = docs.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Consent document not found"));
        apply(d, req);
        docs.save(d);
        return ResponseEntity.ok(toDto(d));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!docs.existsById(id)) throw new ApiProblemException(HttpStatus.NOT_FOUND, "Consent document not found");
        docs.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(ConsentDocument d, ConsentDocumentUpsertRequestDto req) {
        d.setType(req.getType());
        d.setLanguage(req.getLanguage().trim().toLowerCase());
        d.setVersion(req.getVersion().trim());
        d.setActive(Boolean.TRUE.equals(req.getActive()));
        d.setText(req.getText());
    }

    private ConsentDocumentDto toDto(ConsentDocument d) {
        return ConsentDocumentDto.builder()
            .id(d.getId())
            .type(d.getType())
            .language(d.getLanguage())
            .version(d.getVersion())
            .active(d.isActive())
            .text(d.getText())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .build();
    }
}


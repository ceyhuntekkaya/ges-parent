package com.genixo.ges.api.legal;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.legal.dto.ConsentAcceptanceDto;
import com.genixo.ges.api.legal.dto.ConsentAcceptanceRequestDto;
import com.genixo.ges.api.legal.dto.ConsentDocumentDto;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.legal.model.ConsentAcceptance;
import com.genixo.ges.legal.model.ConsentDocument;
import com.genixo.ges.legal.model.ConsentType;
import com.genixo.ges.legal.repo.ConsentAcceptanceRepository;
import com.genixo.ges.legal.repo.ConsentDocumentRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/consents")
public class PortalConsentController {

    private final ConsentDocumentRepository docs;
    private final ConsentAcceptanceRepository acceptances;
    private final UserAccountRepository users;

    public PortalConsentController(
        ConsentDocumentRepository docs,
        ConsentAcceptanceRepository acceptances,
        UserAccountRepository users
    ) {
        this.docs = docs;
        this.acceptances = acceptances;
        this.users = users;
    }

    @GetMapping("/active")
    @Operation(operationId = "portalConsentsListActive")
    public ResponseEntity<PageDto<ConsentDocumentDto>> listActive(
        @RequestParam(required = false) ConsentType type,
        @RequestParam(defaultValue = "tr") String language,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = (type == null) ? docs.findByActiveTrue(pageable) : docs.findByTypeAndActiveTrue(type, pageable);

        var items = p.getContent().stream()
            .filter(d -> d.getLanguage() != null && d.getLanguage().equalsIgnoreCase(language))
            .map(this::toDocDto)
            .toList();

        return ResponseEntity.ok(PageDto.<ConsentDocumentDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/required")
    @Operation(operationId = "portalConsentsRequired")
    public ResponseEntity<List<ConsentDocumentDto>> required(
        @RequestParam(defaultValue = "tr") String language
    ) {
        // MVP: latest active doc per type+language
        return ResponseEntity.ok(
            List.of(ConsentType.values()).stream()
                .map(t -> docs.findFirstByTypeAndLanguageAndActiveTrueOrderByCreatedAtDesc(t, language).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::toDocDto)
                .toList()
        );
    }

    @PostMapping("/accept")
    @Transactional
    @Operation(operationId = "portalConsentsAccept")
    public ResponseEntity<ConsentAcceptanceDto> accept(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody ConsentAcceptanceRequestDto req,
        HttpServletRequest http
    ) {
        ConsentDocument d = docs.findById(req.getConsentDocumentId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid consentDocumentId"));

        var user = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        // idempotency: if already accepted same doc, return latest
        Optional<ConsentAcceptance> existing = acceptances.findFirstByUser_IdAndDocument_IdOrderByAcceptedAtDesc(user.getId(), d.getId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(toAcceptanceDto(existing.get()));
        }

        ConsentAcceptance a = new ConsentAcceptance();
        a.setUser(user);
        a.setDocument(d);
        a.setAcceptedAt(Instant.now());
        a.setIpAddress(Optional.ofNullable(http.getRemoteAddr()).orElse("unknown"));
        a.setUserAgent(Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown"));
        a.setModule(req.getModule());
        a.setApplicationId(req.getApplicationId());
        acceptances.save(a);

        return ResponseEntity.status(HttpStatus.CREATED).body(toAcceptanceDto(a));
    }

    private ConsentDocumentDto toDocDto(ConsentDocument d) {
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

    private ConsentAcceptanceDto toAcceptanceDto(ConsentAcceptance a) {
        return ConsentAcceptanceDto.builder()
            .id(a.getId())
            .userId(a.getUser() == null ? null : a.getUser().getId())
            .consentDocumentId(a.getDocument() == null ? null : a.getDocument().getId())
            .acceptedAt(a.getAcceptedAt())
            .ipAddress(a.getIpAddress())
            .userAgent(a.getUserAgent())
            .module(a.getModule())
            .applicationId(a.getApplicationId())
            .build();
    }
}


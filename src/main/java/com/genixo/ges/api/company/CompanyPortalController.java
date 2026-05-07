package com.genixo.ges.api.company;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.company.dto.CompanyUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.CompanyDto;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.company.model.Company;
import com.genixo.ges.company.repo.CompanyRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/v1/portal/companies")
public class CompanyPortalController {

    private final CompanyRepository companies;
    private final UserAccountRepository users;

    public CompanyPortalController(CompanyRepository companies, UserAccountRepository users) {
        this.companies = companies;
        this.users = users;
    }

    @GetMapping
    @Operation(operationId = "portalCompaniesListMine")
    public ResponseEntity<PageDto<CompanyDto>> listMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q == null || q.isBlank())
            ? companies.findByOwner_Id(principal.getId(), pageable)
            : companies.findByOwner_IdAndNameContainingIgnoreCase(principal.getId(), q, pageable);

        return ResponseEntity.ok(PageDto.<CompanyDto>builder()
            .items(p.getContent().stream().map(this::toDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "portalCompaniesGetMine")
    public ResponseEntity<CompanyDto> getMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        Company c = companies.findByIdAndOwner_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Company not found"));
        return ResponseEntity.ok(toDto(c));
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "portalCompaniesCreate")
    public ResponseEntity<CompanyDto> create(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody CompanyUpsertRequestDto req
    ) {
        UserAccount owner = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        Company c = new Company();
        c.setOwner(owner);
        apply(c, req);
        companies.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(c));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalCompaniesUpdate")
    public ResponseEntity<CompanyDto> update(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody CompanyUpsertRequestDto req
    ) {
        Company c = companies.findByIdAndOwner_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Company not found"));

        apply(c, req);
        companies.save(c);
        return ResponseEntity.ok(toDto(c));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalCompaniesDelete")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        Company c = companies.findByIdAndOwner_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Company not found"));
        companies.delete(c);
        return ResponseEntity.noContent().build();
    }

    private void apply(Company c, CompanyUpsertRequestDto req) {
        c.setName(req.getName().trim());
        c.setTaxNumber(req.getTaxNumber());
        c.setContactFullName(req.getContactFullName());
        c.setContactPhone(req.getContactPhone());
        c.setContactEmail(req.getContactEmail());
    }

    private CompanyDto toDto(Company c) {
        return CompanyDto.builder()
            .id(c.getId())
            .ownerUserId(c.getOwner() == null ? null : c.getOwner().getId())
            .name(c.getName())
            .taxNumber(c.getTaxNumber())
            .contactFullName(c.getContactFullName())
            .contactPhone(c.getContactPhone())
            .contactEmail(c.getContactEmail())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}


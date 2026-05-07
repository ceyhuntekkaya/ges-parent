package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationCreateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationUpdateRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/language-camp-applications")
public class LanguageCampApplicationPortalController {

    private final LanguageCampApplicationRepository apps;
    private final UserAccountRepository users;

    public LanguageCampApplicationPortalController(LanguageCampApplicationRepository apps, UserAccountRepository users) {
        this.apps = apps;
        this.users = users;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<LanguageCampApplicationDetailDto> createDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody LanguageCampApplicationCreateRequestDto req
    ) {
        UserAccount applicant = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        LanguageCampApplication a = new LanguageCampApplication();
        a.setApplicant(applicant);
        a.setCategory(req.getCategory());
        a.setStatus(ApplicationStatus.DRAFT);
        apps.save(a);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(a));
    }

    @GetMapping
    public ResponseEntity<PageDto<LanguageCampApplicationListItemDto>> myList(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = apps.findByApplicant_Id(principal.getId(), pageable);
        var items = p.getContent().stream().map(this::toListItemDto).toList();

        return ResponseEntity.ok(PageDto.<LanguageCampApplicationListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageCampApplicationDetailDto> getMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampApplication a = apps.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(toDetailDto(a));
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<LanguageCampApplicationDetailDto> updateDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationUpdateRequestDto req
    ) {
        LanguageCampApplication a = apps.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        if (a.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be updated");
        }

        if (req.getCategory() != null) a.setCategory(req.getCategory());
        // programId mapping will be added once ProgramRepository exists; keep ID in DTO for now
        if (req.getStartDate() != null) a.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) a.setEndDate(req.getEndDate());
        if (req.getAccommodationType() != null) a.setAccommodationType(req.getAccommodationType());
        if (req.getVisaNeeded() != null) a.setVisaNeeded(req.getVisaNeeded());
        if (req.getVisaFollowByGes() != null) a.setVisaFollowByGes(req.getVisaFollowByGes());
        if (req.getEmergencyContact() != null) a.setEmergencyContact(req.getEmergencyContact());
        if (req.getPaymentPreference() != null) a.setPaymentPreference(req.getPaymentPreference());

        if (req.getCompanyName() != null) a.setCompanyName(req.getCompanyName());
        if (req.getTaxNumber() != null) a.setTaxNumber(req.getTaxNumber());
        if (req.getCompanyContactFullName() != null) a.setCompanyContactFullName(req.getCompanyContactFullName());
        if (req.getCompanyContactPhone() != null) a.setCompanyContactPhone(req.getCompanyContactPhone());
        if (req.getCompanyContactEmail() != null) a.setCompanyContactEmail(req.getCompanyContactEmail());

        apps.save(a);
        return ResponseEntity.ok(toDetailDto(a));
    }

    @PostMapping("/{id}/submit")
    @Transactional
    public ResponseEntity<LanguageCampApplicationDetailDto> submit(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampApplication a = apps.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        if (a.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be submitted");
        }

        a.setStatus(ApplicationStatus.SUBMITTED);
        apps.save(a);
        return ResponseEntity.ok(toDetailDto(a));
    }

    private LanguageCampApplicationListItemDto toListItemDto(LanguageCampApplication a) {
        return LanguageCampApplicationListItemDto.builder()
            .id(a.getId())
            .status(a.getStatus())
            .category(a.getCategory())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }

    private LanguageCampApplicationDetailDto toDetailDto(LanguageCampApplication a) {
        return LanguageCampApplicationDetailDto.builder()
            .id(a.getId())
            .status(a.getStatus())
            .category(a.getCategory())
            .programId(a.getProgram() == null ? null : a.getProgram().getId())
            .startDate(a.getStartDate())
            .endDate(a.getEndDate())
            .accommodationType(a.getAccommodationType())
            .visaNeeded(a.getVisaNeeded())
            .visaFollowByGes(a.getVisaFollowByGes())
            .emergencyContact(a.getEmergencyContact())
            .paymentPreference(a.getPaymentPreference())
            .kvkkAcceptedAt(a.getKvkkAcceptedAt())
            .companyName(a.getCompanyName())
            .taxNumber(a.getTaxNumber())
            .companyContactFullName(a.getCompanyContactFullName())
            .companyContactPhone(a.getCompanyContactPhone())
            .companyContactEmail(a.getCompanyContactEmail())
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }
}


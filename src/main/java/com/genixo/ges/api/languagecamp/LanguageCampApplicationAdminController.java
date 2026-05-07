package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.university.dto.ApplicationStatusChangeRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/language-camp-applications")
public class LanguageCampApplicationAdminController {

    private final LanguageCampApplicationRepository apps;

    public LanguageCampApplicationAdminController(LanguageCampApplicationRepository apps) {
        this.apps = apps;
    }

    @GetMapping
    @Operation(operationId = "adminLanguageCampApplicationsList")
    public ResponseEntity<PageDto<com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ApplicationStatus status
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = apps.findAll(pageable);
        var items = p.getContent().stream()
            .filter(a -> status == null || a.getStatus() == status)
            .map(a -> com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto.builder()
                .id(a.getId())
                .status(a.getStatus())
                .category(a.getCategory())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build())
            .toList();

        return ResponseEntity.ok(PageDto.<com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "adminLanguageCampApplicationsGet")
    public ResponseEntity<com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto> get(@PathVariable UUID id) {
        LanguageCampApplication a = apps.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto.builder()
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
            .build());
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsChangeStatus")
    public ResponseEntity<com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto> changeStatus(
        @PathVariable UUID id,
        @RequestBody ApplicationStatusChangeRequestDto req
    ) {
        LanguageCampApplication a = apps.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        a.setStatus(req.getStatus());
        apps.save(a);
        return get(id);
    }
}


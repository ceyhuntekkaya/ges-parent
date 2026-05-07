package com.genixo.ges.api.university;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.university.dto.ApplicationStatusChangeRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDetailDto;
import com.genixo.ges.api.university.dto.UniversityApplicationListItemDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/v1/admin/university-applications")
public class UniversityApplicationAdminController {

    private final UniversityApplicationRepository applications;

    public UniversityApplicationAdminController(UniversityApplicationRepository applications) {
        this.applications = applications;
    }

    @GetMapping
    @Operation(operationId = "adminUniversityApplicationsList")
    public ResponseEntity<PageDto<UniversityApplicationListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ApplicationStatus status
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = applications.findAll(pageable);
        var items = p.getContent().stream()
            .filter(a -> status == null || a.getStatus() == status)
            .map(this::toListItemDto)
            .toList();

        return ResponseEntity.ok(PageDto.<UniversityApplicationListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "adminUniversityApplicationsGet")
    public ResponseEntity<UniversityApplicationDetailDto> get(@PathVariable UUID id) {
        UniversityApplication ua = applications.findById(id)
            .orElseThrow(() -> new ApiProblemException(org.springframework.http.HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsChangeStatus")
    public ResponseEntity<UniversityApplicationDetailDto> changeStatus(
        @PathVariable UUID id,
        @Valid @RequestBody ApplicationStatusChangeRequestDto req
    ) {
        UniversityApplication ua = applications.findById(id)
            .orElseThrow(() -> new ApiProblemException(org.springframework.http.HttpStatus.NOT_FOUND, "Application not found"));
        ua.setStatus(req.getStatus());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    private UniversityApplicationListItemDto toListItemDto(UniversityApplication ua) {
        return UniversityApplicationListItemDto.builder()
            .id(ua.getId())
            .status(ua.getStatus())
            .educationLevel(ua.getEducationLevel())
            .createdAt(ua.getCreatedAt())
            .updatedAt(ua.getUpdatedAt())
            .build();
    }

    private UniversityApplicationDetailDto toDetailDto(UniversityApplication ua) {
        return UniversityApplicationDetailDto.builder()
            .id(ua.getId())
            .status(ua.getStatus())
            .educationLevel(ua.getEducationLevel())
            .departmentPreferences(ua.getDepartmentPreferences())
            .countryPreferences(ua.getCountryPreferences())
            .universityPreferences(ua.getUniversityPreferences())
            .startTermSeason(ua.getStartTermSeason())
            .startYear(ua.getStartYear())
            .yearlyBudgetMin(ua.getYearlyBudgetMin())
            .yearlyBudgetMax(ua.getYearlyBudgetMax())
            .scholarshipRequested(ua.getScholarshipRequested())
            .scholarshipType(ua.getScholarshipType())
            .accommodationType(ua.getAccommodationType())
            .notes(ua.getNotes())
            .preferencesCompletedAt(ua.getPreferencesCompletedAt())
            .createdAt(ua.getCreatedAt())
            .updatedAt(ua.getUpdatedAt())
            .build();
    }
}


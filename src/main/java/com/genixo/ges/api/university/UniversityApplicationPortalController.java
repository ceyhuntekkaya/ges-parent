package com.genixo.ges.api.university;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.university.dto.UniversityApplicationCreateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDetailDto;
import com.genixo.ges.api.university.dto.UniversityApplicationListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationUpdateRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import jakarta.validation.Valid;
import java.time.Instant;
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
@RequestMapping("/v1/portal/university-applications")
public class UniversityApplicationPortalController {

    private final UniversityApplicationRepository applications;
    private final UserAccountRepository users;

    public UniversityApplicationPortalController(UniversityApplicationRepository applications, UserAccountRepository users) {
        this.applications = applications;
        this.users = users;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<UniversityApplicationDetailDto> createDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody UniversityApplicationCreateRequestDto req
    ) {
        UserAccount applicant = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        UniversityApplication ua = new UniversityApplication();
        ua.setApplicant(applicant);
        ua.setEducationLevel(req.getEducationLevel());
        ua.setStatus(ApplicationStatus.DRAFT);
        applications.save(ua);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(ua));
    }

    @GetMapping
    public ResponseEntity<PageDto<UniversityApplicationListItemDto>> myList(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = applications.findByApplicant_Id(principal.getId(), pageable);

        return ResponseEntity.ok(PageDto.<UniversityApplicationListItemDto>builder()
            .items(p.getContent().stream().map(this::toListItemDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UniversityApplicationDetailDto> getMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<UniversityApplicationDetailDto> updateDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationUpdateRequestDto req
    ) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        if (ua.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be updated");
        }

        if (req.getEducationLevel() != null) ua.setEducationLevel(req.getEducationLevel());
        if (req.getDepartmentPreferences() != null) ua.setDepartmentPreferences(req.getDepartmentPreferences());
        if (req.getCountryPreferences() != null) ua.setCountryPreferences(req.getCountryPreferences());
        if (req.getUniversityPreferences() != null) ua.setUniversityPreferences(req.getUniversityPreferences());
        if (req.getStartTermSeason() != null) ua.setStartTermSeason(req.getStartTermSeason());
        if (req.getStartYear() != null) ua.setStartYear(req.getStartYear());
        if (req.getYearlyBudgetMin() != null) ua.setYearlyBudgetMin(req.getYearlyBudgetMin());
        if (req.getYearlyBudgetMax() != null) ua.setYearlyBudgetMax(req.getYearlyBudgetMax());
        if (req.getScholarshipRequested() != null) ua.setScholarshipRequested(req.getScholarshipRequested());
        if (req.getScholarshipType() != null) ua.setScholarshipType(req.getScholarshipType());
        if (req.getAccommodationType() != null) ua.setAccommodationType(req.getAccommodationType());
        if (req.getNotes() != null) ua.setNotes(req.getNotes());

        // mark preferences completed if sufficient data exists (light heuristic)
        if (ua.getPreferencesCompletedAt() == null
            && ua.getDepartmentPreferences() != null && !ua.getDepartmentPreferences().isEmpty()
            && ua.getCountryPreferences() != null && !ua.getCountryPreferences().isEmpty()) {
            ua.setPreferencesCompletedAt(Instant.now());
        }

        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/submit")
    @Transactional
    public ResponseEntity<UniversityApplicationDetailDto> submit(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        if (ua.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be submitted");
        }

        ua.setStatus(ApplicationStatus.SUBMITTED);
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


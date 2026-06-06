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
import com.genixo.ges.company.model.Company;
import com.genixo.ges.company.repo.CompanyRepository;
import com.genixo.ges.languagecamp.LanguageCampApplicationFeeSupport;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampProjectRepository;
import com.genixo.ges.languagecamp.service.LanguageCampVisaFormService;
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
    private final LanguageCampProjectRepository projects;
    private final UserAccountRepository users;
    private final CompanyRepository companies;
    private final LanguageCampVisaFormService visaForms;

    public LanguageCampApplicationPortalController(
        LanguageCampApplicationRepository apps,
        LanguageCampProjectRepository projects,
        UserAccountRepository users,
        CompanyRepository companies,
        LanguageCampVisaFormService visaForms
    ) {
        this.apps = apps;
        this.projects = projects;
        this.users = users;
        this.companies = companies;
        this.visaForms = visaForms;
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "portalLanguageCampApplicationsCreateDraft")
    public ResponseEntity<LanguageCampApplicationDetailDto> createDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody LanguageCampApplicationCreateRequestDto req
    ) {
        UserAccount applicant = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        LanguageCampProject project = projects.findById(req.getLanguageCampProjectId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid languageCampProjectId"));

        LanguageCampApplication a = new LanguageCampApplication();
        a.setApplicant(applicant);
        a.setLanguageCampProject(project);
        LanguageCampApplicationFeeSupport.applyFromProject(a, project);
        a.setCategory(req.getCategory());
        a.setStatus(ApplicationStatus.DRAFT);
        a.setFirstName(req.getFirstName());
        a.setLastName(req.getLastName());
        a.setBirthDate(req.getBirthDate());
        a.setPhone(req.getPhone());
        a.setIsItSelf(req.getIsItSelf());
        a.setNumberOfApplicant(req.getNumberOfApplicant());
        a.setUnder18(req.getUnder18());
        a.setParentFullName(req.getParentFullName());
        a.setParentPhoneNumber(req.getParentPhoneNumber());
        a.setParentEmailAddress(req.getParentEmailAddress());
        a.setParentRelationship(req.getParentRelationship());
        a.setUserNotes(req.getUserNotes());
        apps.save(a);
        a.setVisaForm(visaForms.createForApplication(a));

        return ResponseEntity.status(HttpStatus.CREATED).body(LanguageCampApplicationDtoMapper.toDetailDto(
            apps.findDetailByIdAndApplicant_Id(a.getId(), principal.getId()).orElse(a)
        ));
    }

    @GetMapping
    @Operation(operationId = "portalLanguageCampApplicationsListMine")
    public ResponseEntity<PageDto<LanguageCampApplicationListItemDto>> myList(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = apps.findByApplicant_Id(principal.getId(), pageable);
        return ResponseEntity.ok(PageDto.<LanguageCampApplicationListItemDto>builder()
            .items(p.getContent().stream().map(LanguageCampApplicationDtoMapper::toListItemDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(operationId = "portalLanguageCampApplicationsGetMine")
    public ResponseEntity<LanguageCampApplicationDetailDto> getMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampApplication a = apps.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(LanguageCampApplicationDtoMapper.toDetailDto(a));
    }

    @PatchMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalLanguageCampApplicationsUpdateDraft")
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
        if (req.getLanguageCampProjectId() != null) {
            LanguageCampProject project = projects.findById(req.getLanguageCampProjectId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid languageCampProjectId"));
            a.setLanguageCampProject(project);
            LanguageCampApplicationFeeSupport.applyFromProject(a, project);
        }
        if (req.getAccommodationType() != null) a.setAccommodationType(req.getAccommodationType());
        if (req.getVisaNeeded() != null) a.setVisaNeeded(req.getVisaNeeded());
        if (req.getVisaFollowByGes() != null) a.setVisaFollowByGes(req.getVisaFollowByGes());
        if (req.getEmergencyContact() != null) a.setEmergencyContact(req.getEmergencyContact());
        if (req.getPaymentPreference() != null) a.setPaymentPreference(req.getPaymentPreference());
        if (req.getFirstName() != null) a.setFirstName(req.getFirstName());
        if (req.getLastName() != null) a.setLastName(req.getLastName());
        if (req.getBirthDate() != null) a.setBirthDate(req.getBirthDate());
        if (req.getPhone() != null) a.setPhone(req.getPhone());
        if (req.getIsItSelf() != null) a.setIsItSelf(req.getIsItSelf());
        if (req.getNumberOfApplicant() != null) a.setNumberOfApplicant(req.getNumberOfApplicant());
        if (req.getUnder18() != null) a.setUnder18(req.getUnder18());
        if (req.getParentFullName() != null) a.setParentFullName(req.getParentFullName());
        if (req.getParentPhoneNumber() != null) a.setParentPhoneNumber(req.getParentPhoneNumber());
        if (req.getParentEmailAddress() != null) a.setParentEmailAddress(req.getParentEmailAddress());
        if (req.getParentRelationship() != null) a.setParentRelationship(req.getParentRelationship());
        if (req.getUserNotes() != null) a.setUserNotes(req.getUserNotes());

        if (req.getCompanyCode() != null && !req.getCompanyCode().isBlank()) {
            Company c = companies.findByCode(req.getCompanyCode().trim())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid companyCode"));
            a.setCompany(c);
        }

        apps.save(a);
        return ResponseEntity.ok(LanguageCampApplicationDtoMapper.toDetailDto(
            apps.findDetailByIdAndApplicant_Id(a.getId(), principal.getId()).orElse(a)
        ));
    }

    @PostMapping("/{id}/submit")
    @Transactional
    @Operation(operationId = "portalLanguageCampApplicationsSubmit")
    public ResponseEntity<LanguageCampApplicationDetailDto> submit(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampApplication a = apps.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));

        if (a.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be submitted");
        }
        if (a.getLanguageCampProject() == null) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "languageCampProjectId is required");
        }

        a.setStatus(ApplicationStatus.SUBMITTED);
        apps.save(a);
        return ResponseEntity.ok(LanguageCampApplicationDtoMapper.toDetailDto(
            apps.findDetailByIdAndApplicant_Id(a.getId(), principal.getId()).orElse(a)
        ));
    }
}


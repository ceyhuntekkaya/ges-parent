package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationGroupDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampParticipantCreateRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.languagecamp.LanguageCampApplicationFeeSupport;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampProjectRepository;
import com.genixo.ges.languagecamp.service.LanguageCampVisaFormService;
import com.genixo.ges.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/language-camp-application-groups")
public class LanguageCampApplicationGroupPortalController {

    private final LanguageCampApplicationRepository apps;
    private final LanguageCampProjectRepository projects;
    private final UserAccountRepository users;
    private final LanguageCampVisaFormService visaForms;

    public LanguageCampApplicationGroupPortalController(
        LanguageCampApplicationRepository apps,
        LanguageCampProjectRepository projects,
        UserAccountRepository users,
        LanguageCampVisaFormService visaForms
    ) {
        this.apps = apps;
        this.projects = projects;
        this.users = users;
        this.visaForms = visaForms;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(operationId = "portalLanguageCampApplicationGroupsListMine")
    public ResponseEntity<List<LanguageCampApplicationGroupDto>> listMine(
        @AuthenticationPrincipal AuthUserPrincipal principal
    ) {
        List<LanguageCampApplication> mine = apps.findByApplicant_IdOrderByCreatedAtDesc(principal.getId());
        Map<UUID, List<LanguageCampApplication>> byProject = new LinkedHashMap<>();
        for (LanguageCampApplication a : mine) {
            if (a.getLanguageCampProject() == null || a.getLanguageCampProject().getId() == null) continue;
            byProject.computeIfAbsent(a.getLanguageCampProject().getId(), k -> new ArrayList<>()).add(a);
        }

        List<LanguageCampApplicationGroupDto> groups = new ArrayList<>();
        for (var entry : byProject.entrySet()) {
            UUID projectId = entry.getKey();
            LanguageCampProject project = projects.findById(projectId)
                .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found"));

            List<LanguageCampApplicationDetailDto> participants = entry.getValue().stream()
                .sorted(Comparator.comparing(LanguageCampApplication::getCreatedAt))
                .map(a -> apps.findDetailByIdAndApplicant_Id(a.getId(), principal.getId()).orElse(a))
                .map(LanguageCampApplicationDtoMapper::toDetailDto)
                .toList();

            groups.add(LanguageCampApplicationGroupDto.builder()
                .projectId(projectId)
                .project(LanguageCampProjectDtoMapper.toDetailDto(project))
                .participants(participants)
                .build());
        }

        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{projectId}/participants")
    @Transactional
    @Operation(operationId = "portalLanguageCampApplicationGroupsAddParticipant")
    public ResponseEntity<LanguageCampApplicationDetailDto> addParticipant(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID projectId,
        @Valid @RequestBody(required = false) LanguageCampParticipantCreateRequestDto req
    ) {
        UserAccount applicant = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        LanguageCampProject project = projects.findById(projectId)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found"));

        List<LanguageCampApplication> siblings = apps.findByApplicant_IdAndLanguageCampProject_IdOrderByCreatedAtAsc(
            principal.getId(),
            projectId
        );

        LanguageCampCategory category = siblings.isEmpty()
            ? LanguageCampCategory.INDIVIDUAL
            : siblings.get(0).getCategory();

        LanguageCampApplication a = new LanguageCampApplication();
        a.setApplicant(applicant);
        a.setLanguageCampProject(project);
        LanguageCampApplicationFeeSupport.applyFromProject(a, project);
        a.setCategory(category);
        a.setStatus(ApplicationStatus.DRAFT);
        a.setIsItSelf(false);
        a.setNumberOfApplicant(siblings.size() + 1);

        if (!siblings.isEmpty()) {
            LanguageCampApplication ref = siblings.get(0);
            a.setAccommodationType(ref.getAccommodationType());
            a.setVisaNeeded(ref.getVisaNeeded());
            a.setVisaFollowByGes(ref.getVisaFollowByGes());
            a.setPaymentPreference(ref.getPaymentPreference());
            a.setEmergencyContact(ref.getEmergencyContact());
            a.setCompany(ref.getCompany());
        }

        if (req != null) {
            if (req.getFirstName() != null) a.setFirstName(req.getFirstName());
            if (req.getLastName() != null) a.setLastName(req.getLastName());
            if (req.getBirthDate() != null) a.setBirthDate(req.getBirthDate());
            if (req.getPhone() != null) a.setPhone(req.getPhone());
            if (req.getIsItSelf() != null) a.setIsItSelf(req.getIsItSelf());
            if (req.getUnder18() != null) a.setUnder18(req.getUnder18());
            if (req.getParentFullName() != null) a.setParentFullName(req.getParentFullName());
            if (req.getParentPhoneNumber() != null) a.setParentPhoneNumber(req.getParentPhoneNumber());
            if (req.getParentEmailAddress() != null) a.setParentEmailAddress(req.getParentEmailAddress());
            if (req.getParentRelationship() != null) a.setParentRelationship(req.getParentRelationship());
            if (req.getUserNotes() != null) a.setUserNotes(req.getUserNotes());
            if (req.getAccommodationType() != null) a.setAccommodationType(req.getAccommodationType());
            if (req.getVisaNeeded() != null) a.setVisaNeeded(req.getVisaNeeded());
            if (req.getVisaFollowByGes() != null) a.setVisaFollowByGes(req.getVisaFollowByGes());
            if (req.getPaymentPreference() != null) a.setPaymentPreference(req.getPaymentPreference());
            if (req.getEmergencyContact() != null) a.setEmergencyContact(req.getEmergencyContact());
        }

        apps.save(a);
        a.setVisaForm(visaForms.createForApplication(a));

        return ResponseEntity.status(HttpStatus.CREATED).body(LanguageCampApplicationDtoMapper.toDetailDto(
            apps.findDetailByIdAndApplicant_Id(a.getId(), principal.getId()).orElse(a)
        ));
    }
}

package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormUpsertRequestDto;
import com.genixo.ges.languagecamp.model.LanguageCampParticipant;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.repo.LanguageCampParticipantRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.storage.repo.StoredFileRepository;
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
@RequestMapping("/v1/portal/language-camp-visa-forms")
public class LanguageCampVisaFormPortalController {

    private final LanguageCampVisaFormRepository forms;
    private final LanguageCampParticipantRepository participants;
    private final StoredFileRepository storedFiles;

    public LanguageCampVisaFormPortalController(
        LanguageCampVisaFormRepository forms,
        LanguageCampParticipantRepository participants,
        StoredFileRepository storedFiles
    ) {
        this.forms = forms;
        this.participants = participants;
        this.storedFiles = storedFiles;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<LanguageCampVisaFormDto> create(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody LanguageCampVisaFormUpsertRequestDto req
    ) {
        if (req.getParticipantId() == null) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "participantId is required");
        }

        LanguageCampParticipant p = participants.findByIdAndApplication_Applicant_Id(req.getParticipantId(), principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Participant not found"));

        forms.findByParticipant_Id(p.getId()).ifPresent(x -> {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Visa form already exists for participant");
        });

        LanguageCampVisaForm f = new LanguageCampVisaForm();
        f.setParticipant(p);
        apply(f, req, principal.getId());
        forms.save(f);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(f));
    }

    @GetMapping
    public ResponseEntity<PageDto<LanguageCampVisaFormDto>> listByApplication(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @RequestParam UUID applicationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        // ownership: participant.application.applicant must be current user -> enforced by filtering participants not easily in query
        // We'll rely on participant repository check per item by joining application in entity graph at runtime (LAZY); keep simple now.
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = forms.findAllByParticipant_Application_Id(applicationId, pageable);
        var items = p.getContent().stream()
            .filter(x -> x.getParticipant() != null
                && x.getParticipant().getApplication() != null
                && x.getParticipant().getApplication().getApplicant() != null
                && principal.getId().equals(x.getParticipant().getApplication().getApplicant().getId()))
            .map(this::toDto)
            .toList();

        return ResponseEntity.ok(PageDto.<LanguageCampVisaFormDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageCampVisaFormDto> get(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        if (f.getParticipant() == null
            || f.getParticipant().getApplication() == null
            || f.getParticipant().getApplication().getApplicant() == null
            || !principal.getId().equals(f.getParticipant().getApplication().getApplicant().getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return ResponseEntity.ok(toDto(f));
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<LanguageCampVisaFormDto> update(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampVisaFormUpsertRequestDto req
    ) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));

        if (f.getParticipant() == null
            || f.getParticipant().getApplication() == null
            || f.getParticipant().getApplication().getApplicant() == null
            || !principal.getId().equals(f.getParticipant().getApplication().getApplicant().getId())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        apply(f, req, principal.getId());
        forms.save(f);
        return ResponseEntity.ok(toDto(f));
    }

    private void apply(LanguageCampVisaForm f, LanguageCampVisaFormUpsertRequestDto req, UUID currentUserId) {
        if (req.getBirthPlace() != null) f.setBirthPlace(req.getBirthPlace());
        if (req.getBirthCountry() != null) f.setBirthCountry(req.getBirthCountry());
        if (req.getResidenceAddress() != null) f.setResidenceAddress(req.getResidenceAddress());
        if (req.getVisaRejectedBefore() != null) f.setVisaRejectedBefore(req.getVisaRejectedBefore());
        if (req.getVisaRejectionDetails() != null) f.setVisaRejectionDetails(req.getVisaRejectionDetails());
        if (req.getVisitedCountries() != null) f.setVisitedCountries(req.getVisitedCountries());
        if (req.getAppointmentCityPreference() != null) f.setAppointmentCityPreference(req.getAppointmentCityPreference());

        if (req.getBankStatementFileId() != null) {
            StoredFile sf = storedFiles.findById(req.getBankStatementFileId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid bankStatementFileId"));
            if (sf.getUploadedBy() == null || !currentUserId.equals(sf.getUploadedBy().getId())) {
                throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            f.setBankStatementFile(sf);
        }

        if (req.getBiometricPhotoFileId() != null) {
            StoredFile sf = storedFiles.findById(req.getBiometricPhotoFileId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid biometricPhotoFileId"));
            if (sf.getUploadedBy() == null || !currentUserId.equals(sf.getUploadedBy().getId())) {
                throw new ApiProblemException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            f.setBiometricPhotoFile(sf);
        }
    }

    private LanguageCampVisaFormDto toDto(LanguageCampVisaForm f) {
        return LanguageCampVisaFormDto.builder()
            .id(f.getId())
            .participantId(f.getParticipant() == null ? null : f.getParticipant().getId())
            .applicationId(f.getParticipant() == null || f.getParticipant().getApplication() == null ? null : f.getParticipant().getApplication().getId())
            .birthPlace(f.getBirthPlace())
            .birthCountry(f.getBirthCountry())
            .residenceAddress(f.getResidenceAddress())
            .visaRejectedBefore(f.getVisaRejectedBefore())
            .visaRejectionDetails(f.getVisaRejectionDetails())
            .visitedCountries(f.getVisitedCountries())
            .bankStatementFileId(f.getBankStatementFile() == null ? null : f.getBankStatementFile().getId())
            .biometricPhotoFileId(f.getBiometricPhotoFile() == null ? null : f.getBiometricPhotoFile().getId())
            .appointmentCityPreference(f.getAppointmentCityPreference())
            .createdAt(f.getCreatedAt())
            .updatedAt(f.getUpdatedAt())
            .build();
    }
}


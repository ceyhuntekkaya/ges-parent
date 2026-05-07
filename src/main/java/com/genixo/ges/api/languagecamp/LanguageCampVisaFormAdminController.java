package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.languagecamp.model.LanguageCampVisaForm;
import com.genixo.ges.languagecamp.repo.LanguageCampVisaFormRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/language-camp-visa-forms")
public class LanguageCampVisaFormAdminController {

    private final LanguageCampVisaFormRepository forms;

    public LanguageCampVisaFormAdminController(LanguageCampVisaFormRepository forms) {
        this.forms = forms;
    }

    @GetMapping
    public ResponseEntity<PageDto<LanguageCampVisaFormDto>> listByApplication(
        @RequestParam UUID applicationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = forms.findAllByParticipant_Application_Id(applicationId, pageable);
        var items = p.getContent().stream().map(this::toDto).toList();

        return ResponseEntity.ok(PageDto.<LanguageCampVisaFormDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageCampVisaFormDto> get(@PathVariable UUID id) {
        LanguageCampVisaForm f = forms.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Visa form not found"));
        return ResponseEntity.ok(toDto(f));
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


package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.PublicLanguageCampApplicationCompleteRequestDto;
import com.genixo.ges.api.languagecamp.dto.PublicLanguageCampApplicationCompleteResponseDto;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.applicant.repo.ApplicantProfileRepository;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.model.UserStatus;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.company.model.Company;
import com.genixo.ges.company.repo.CompanyRepository;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public/language-camp-applications")
public class LanguageCampApplicationPublicController {

    private final UserAccountRepository users;
    private final ApplicantProfileRepository profiles;
    private final LanguageCampApplicationRepository apps;
    private final CompanyRepository companies;
    private final PasswordEncoder passwordEncoder;

    public LanguageCampApplicationPublicController(
        UserAccountRepository users,
        ApplicantProfileRepository profiles,
        LanguageCampApplicationRepository apps,
        CompanyRepository companies,
        PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.profiles = profiles;
        this.apps = apps;
        this.companies = companies;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/complete")
    @Transactional
    @Operation(operationId = "publicLanguageCampApplicationsComplete")
    public ResponseEntity<PublicLanguageCampApplicationCompleteResponseDto> complete(
        @Valid @RequestBody PublicLanguageCampApplicationCompleteRequestDto req,
        HttpServletRequest http
    ) {
        String email = req.getAccount().getEmail() == null ? null : req.getAccount().getEmail().trim().toLowerCase();
        if (email == null || email.isBlank()) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Validation failed");
        }
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Email already registered");
        }

        // 1) Create user account (no tokens here; UI will login via /api/auth/login)
        UserAccount ua = new UserAccount();
        ua.setEmail(email);
        ua.setPasswordHash(passwordEncoder.encode(req.getAccount().getPassword()));
        ua.setRole(UserRole.USER);
        ua.setStatus(UserStatus.ACTIVE);
        users.save(ua);

        // 2) Create applicant profile
        ApplicantProfile ap = new ApplicantProfile();
        ap.setUser(ua);
        ap.setFirstName(req.getApplicantProfile().getFirstName());
        ap.setLastName(req.getApplicantProfile().getLastName());
        ap.setBirthDate(req.getApplicantProfile().getBirthDate());
        ap.setPhone(req.getApplicantProfile().getPhone());
        ap.setNationality(req.getApplicantProfile().getNationality());
        ap.setAddress(req.getApplicantProfile().getAddress());
        profiles.save(ap);

        // 3) Create application and mark as submitted (single-step completion)
        LanguageCampApplication a = new LanguageCampApplication();
        a.setApplicant(ua);
        a.setFirstName(req.getApplicantProfile().getFirstName());
        a.setLastName(req.getApplicantProfile().getLastName());
        a.setBirthDate(req.getApplicantProfile().getBirthDate());
        a.setPhone(req.getApplicantProfile().getPhone());
        a.setCategory(req.getApplication().getCategory());
        a.setStatus(ApplicationStatus.SUBMITTED);
        a.setAccommodationType(req.getApplication().getAccommodationType());
        a.setVisaNeeded(req.getApplication().getVisaNeeded());
        a.setVisaFollowByGes(req.getApplication().getVisaFollowByGes());
        a.setEmergencyContact(req.getApplication().getEmergencyContact());
        a.setPaymentPreference(req.getApplication().getPaymentPreference());
        a.setInvoiceAddress(req.getApplication().getInvoiceAddress());
        a.setIsItSelf(req.getApplication().getIsItSelf());
        a.setNumberOfApplicant(req.getApplication().getNumberOfApplicant());
        a.setUnder18(req.getApplication().getUnder18());
        a.setParentFullName(req.getApplication().getParentFullName());
        a.setParentPhoneNumber(req.getApplication().getParentPhoneNumber());
        a.setParentEmailAddress(req.getApplication().getParentEmailAddress());
        a.setParentRelationship(req.getApplication().getParentRelationship());
        a.setUserNotes(req.getApplication().getUserNotes());

        if (Boolean.TRUE.equals(req.getApplication().getKvkkAccepted())) {
            a.setKvkkAcceptedAt(Instant.now());
        }

        if (req.getApplication().getCompanyCode() != null && !req.getApplication().getCompanyCode().isBlank()) {
            Company c = companies.findByCode(req.getApplication().getCompanyCode().trim())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid companyCode"));
            a.setCompany(c);
        }

        apps.save(a);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            PublicLanguageCampApplicationCompleteResponseDto.builder()
                .applicationId(a.getId())
                .build()
        );
    }
}


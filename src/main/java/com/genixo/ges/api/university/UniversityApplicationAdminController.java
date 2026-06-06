package com.genixo.ges.api.university;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.university.dto.ApplicationStatusChangeRequestDto;
import com.genixo.ges.api.university.dto.PortfolioSectionDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDocumentUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationAdminCreateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDetailDto;
import com.genixo.ges.api.university.dto.PendingTaskListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationByStatusListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationMeetingUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationNoteCreateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationNoteUpdateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPaymentUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPortfolioFileUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPortfolioSectionUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationStringListItemUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationTaskUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationUpdateRequestDto;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.applicant.repo.ApplicantProfileRepository;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.model.UserStatus;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.common.jpa.Address;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import com.genixo.ges.university.model.UniversityApplicationMeeting;
import com.genixo.ges.university.model.UniversityApplicationNote;
import com.genixo.ges.university.model.UniversityApplicationPayment;
import com.genixo.ges.university.model.UniversityApplicationPortfolioFile;
import com.genixo.ges.university.model.UniversityApplicationPortfolioSection;
import com.genixo.ges.university.model.UniversityApplicationTask;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import com.genixo.ges.university.repo.PortfolioSectionRepository;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/v1/admin/university-applications")
public class UniversityApplicationAdminController {

    private final UniversityApplicationRepository applications;
    private final PortfolioSectionRepository portfolioSections;
    private final UserAccountRepository users;
    private final ApplicantProfileRepository applicantProfiles;
    private final PasswordEncoder passwordEncoder;
    private final UniversityApplicationDocumentSeeder documentSeeder;
    private final UniversityApplicationPortfolioSeeder portfolioSeeder;

    public UniversityApplicationAdminController(
        UniversityApplicationRepository applications,
        PortfolioSectionRepository portfolioSections,
        UserAccountRepository users,
        ApplicantProfileRepository applicantProfiles,
        PasswordEncoder passwordEncoder,
        UniversityApplicationDocumentSeeder documentSeeder,
        UniversityApplicationPortfolioSeeder portfolioSeeder
    ) {
        this.applications = applications;
        this.portfolioSections = portfolioSections;
        this.users = users;
        this.applicantProfiles = applicantProfiles;
        this.passwordEncoder = passwordEncoder;
        this.documentSeeder = documentSeeder;
        this.portfolioSeeder = portfolioSeeder;
    }

    @GetMapping
    @Transactional(readOnly = true)
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

    @GetMapping("/pending-tasks")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminUniversityApplicationsPendingTasks")
    public ResponseEntity<List<PendingTaskListItemDto>> listPendingTasks() {
        List<PendingTaskListItemDto> items = applications
            .findAllWithTasksByTaskStatus(UniversityApplicationTaskStatus.PENDING)
            .stream()
            .flatMap(ua -> ua.getTasks().stream()
                .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
                .map(t -> toPendingTaskListItemDto(ua, t)))
            .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-status")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminUniversityApplicationsByStatus")
    public ResponseEntity<List<UniversityApplicationByStatusListItemDto>> listByApplicationStatus(
        @RequestParam ApplicationStatus status
    ) {
        List<UniversityApplicationByStatusListItemDto> items = applications
            .findByStatus(status)
            .stream()
            .map(UniversityApplicationAdminController::toByStatusListItemDto)
            .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminUniversityApplicationsGet")
    public ResponseEntity<UniversityApplicationDetailDto> get(@PathVariable UUID id) {
        UniversityApplication ua = applications.findById(id)
            .orElseThrow(() -> new ApiProblemException(org.springframework.http.HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsCreate")
    public ResponseEntity<UniversityApplicationDetailDto> create(@Valid @RequestBody UniversityApplicationAdminCreateRequestDto req) {
        boolean hasUserId = req.getApplicantUserId() != null;
        boolean hasNew = req.getNewApplicant() != null;
        if (hasUserId == hasNew) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "applicantUserId veya newApplicant alanlarından tam olarak biri dolu olmalıdır.");
        }

        UserAccount applicant;
        if (hasUserId) {
            applicant = users.findById(req.getApplicantUserId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Kullanıcı bulunamadı."));
            if (applicant.getRole() != UserRole.USER) {
                throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Yalnızca USER rolündeki hesaplar başvuru sahibi seçilebilir.");
            }
        } else {
            var na = req.getNewApplicant();
            String email = na.getEmail() == null ? null : na.getEmail().trim().toLowerCase();
            if (email == null || email.isBlank()) {
                throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Geçerli bir e-posta giriniz.");
            }
            if (users.existsByEmailIgnoreCase(email)) {
                throw new ApiProblemException(HttpStatus.CONFLICT, "Bu e-posta ile kayıtlı bir kullanıcı zaten var.");
            }
            applicant = new UserAccount();
            applicant.setEmail(email);
            applicant.setPasswordHash(passwordEncoder.encode(na.getPassword()));
            applicant.setRole(UserRole.USER);
            applicant.setStatus(UserStatus.ACTIVE);
            users.save(applicant);
            createApplicantProfileForNewUserIfNeeded(applicant, req.getInitialSnapshot());
        }

        UniversityApplication ua = new UniversityApplication();
        ua.setApplicant(applicant);
        if (hasUserId) {
            ua.setApplicantProfile(applicant.getApplicantProfile());
        } else {
            applicantProfiles.findByUser_Id(applicant.getId()).ifPresent(ua::setApplicantProfile);
        }
        ua.setEducationLevel(req.getEducationLevel());
        ua.setStatus(req.getStatus() != null ? req.getStatus() : ApplicationStatus.DRAFT);

        applyUniversityApplicationPatch(ua, req.getInitialSnapshot());
        ua.setEducationLevel(req.getEducationLevel());

        if (ua.getEducationLevel() == null) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Eğitim seviyesi zorunludur.");
        }
        if (ua.getStatus() == null) {
            ua.setStatus(ApplicationStatus.DRAFT);
        }

        documentSeeder.seedFromActiveRequirements(ua);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(ua));
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

    @PatchMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationUpdateRequestDto req
    ) {
        UniversityApplication ua = getAny(id);

        applyUniversityApplicationPatch(ua, req);

        // DB seviyesinde zorunlu alanlar null kaldığında Hibernate flush sırasında 500 üretebiliyor.
        // Burada 400 ile net bir hata dönerek istemciyi doğru yönlendirelim.
        if (ua.getEducationLevel() == null) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Eğitim seviyesi zorunludur.");
        }
        if (ua.getStatus() == null) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Başvuru durumu zorunludur.");
        }

        if (req.getEducationLevel() != null) {
            portfolioSeeder.seedMatchingTemplates(ua);
        }

        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/department-preferences")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDepartmentPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addDepartmentPreference(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        list.add(req.getValue());
        ua.setDepartmentPreferences(list);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/department-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDepartmentPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateDepartmentPreference(
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setDepartmentPreferences(list);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/department-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDepartmentPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteDepartmentPreference(
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setDepartmentPreferences(list);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/country-preferences")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsCountryPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addCountryPreference(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        list.add(req.getValue());
        ua.setCountryPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/country-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsCountryPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateCountryPreference(
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setCountryPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/country-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsCountryPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteCountryPreference(
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setCountryPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/university-preferences")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsUniversityPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addUniversityPreference(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        list.add(req.getValue());
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/university-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsUniversityPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateUniversityPreference(
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/university-preferences/{index}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsUniversityPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteUniversityPreference(
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getAny(id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/notes")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsNotesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addNote(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationNoteCreateRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationNote n = new UniversityApplicationNote();
        n.setApplication(ua);
        n.setWrittenBy(principal == null ? "admin" : principal.getEmail());
        n.setWrittenAt(Instant.now());
        n.setTodoText(req.getTodoText());

        List<UniversityApplicationNote> list = ua.getApplicationNotes();
        if (list == null) list = new ArrayList<>();
        list.add(n);
        ua.setApplicationNotes(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/notes/{noteId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsNotesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateNote(
        @PathVariable UUID id,
        @PathVariable UUID noteId,
        @Valid @RequestBody UniversityApplicationNoteUpdateRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationNote n = findByIdOrThrow(ua.getApplicationNotes(), noteId, "Note not found");
        n.setTodoText(req.getTodoText());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/notes/{noteId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsNotesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteNote(
        @PathVariable UUID id,
        @PathVariable UUID noteId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getApplicationNotes(), noteId, "Note not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/meetings")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsMeetingsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addMeeting(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationMeetingUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationMeeting m = new UniversityApplicationMeeting();
        m.setApplication(ua);
        m.setPerson(req.getPerson());
        m.setMeetingAt(req.getMeetingAt());
        m.setMeetingNote(req.getMeetingNote());
        m.setMeetingResult(req.getMeetingResult());

        List<UniversityApplicationMeeting> list = ua.getMeetings();
        if (list == null) list = new ArrayList<>();
        list.add(m);
        ua.setMeetings(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/meetings/{meetingId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsMeetingsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateMeeting(
        @PathVariable UUID id,
        @PathVariable UUID meetingId,
        @Valid @RequestBody UniversityApplicationMeetingUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationMeeting m = findByIdOrThrow(ua.getMeetings(), meetingId, "Meeting not found");
        m.setPerson(req.getPerson());
        m.setMeetingAt(req.getMeetingAt());
        m.setMeetingNote(req.getMeetingNote());
        m.setMeetingResult(req.getMeetingResult());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/meetings/{meetingId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsMeetingsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteMeeting(
        @PathVariable UUID id,
        @PathVariable UUID meetingId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getMeetings(), meetingId, "Meeting not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/tasks")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsTasksAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationTaskUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationTask t = new UniversityApplicationTask();
        t.setApplication(ua);
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        t.setStatus(req.getStatus() == null ? UniversityApplicationTaskStatus.PENDING : req.getStatus());
        if (t.getStatus() == UniversityApplicationTaskStatus.DONE) {
            t.setPerformedByUser(principal == null ? "admin" : principal.getEmail());
        }

        List<UniversityApplicationTask> list = ua.getTasks();
        if (list == null) list = new ArrayList<>();
        list.add(t);
        ua.setTasks(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/tasks/{taskId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsTasksUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID taskId,
        @Valid @RequestBody UniversityApplicationTaskUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationTask t = findByIdOrThrow(ua.getTasks(), taskId, "Task not found");
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
            if (req.getStatus() == UniversityApplicationTaskStatus.DONE && (t.getPerformedByUser() == null || t.getPerformedByUser().isBlank())) {
                t.setPerformedByUser(principal == null ? "admin" : principal.getEmail());
            }
        }
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsTasksDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteTask(
        @PathVariable UUID id,
        @PathVariable UUID taskId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getTasks(), taskId, "Task not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/documents")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDocumentsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addDocument(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationDocumentUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationDocument d = new UniversityApplicationDocument();
        d.setApplication(ua);
        UniversityApplicationDocumentMutations.applyForCreate(d, req);

        List<UniversityApplicationDocument> list = ua.getDocuments();
        if (list == null) list = new ArrayList<>();
        list.add(d);
        ua.setDocuments(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDocumentsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateDocument(
        @PathVariable UUID id,
        @PathVariable UUID documentId,
        @Valid @RequestBody UniversityApplicationDocumentUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationDocument d = findByIdOrThrow(ua.getDocuments(), documentId, "Document not found");
        UniversityApplicationDocumentMutations.applyForUpdate(d, req);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsDocumentsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteDocument(
        @PathVariable UUID id,
        @PathVariable UUID documentId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getDocuments(), documentId, "Document not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/payments")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPaymentsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPayment(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationPaymentUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPayment p = new UniversityApplicationPayment();
        p.setApplication(ua);
        p.setPaymentAt(req.getPaymentAt());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setReceivedBy(req.getReceivedBy());

        List<UniversityApplicationPayment> list = ua.getPayments();
        if (list == null) list = new ArrayList<>();
        list.add(p);
        ua.setPayments(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/payments/{paymentId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPaymentsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePayment(
        @PathVariable UUID id,
        @PathVariable UUID paymentId,
        @Valid @RequestBody UniversityApplicationPaymentUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPayment p = findByIdOrThrow(ua.getPayments(), paymentId, "Payment not found");
        p.setPaymentAt(req.getPaymentAt());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setReceivedBy(req.getReceivedBy());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPaymentsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePayment(
        @PathVariable UUID id,
        @PathVariable UUID paymentId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getPayments(), paymentId, "Payment not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/portfolio-sections")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioSectionsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPortfolioSection(
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationPortfolioSectionUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPortfolioSection s = new UniversityApplicationPortfolioSection();
        s.setApplication(ua);
        s.setRequired(req.getRequired());
        s.setSortOrder(req.getSortOrder());
        s.setSectionNameOverride(req.getSectionNameOverride());
        s.setSectionDescriptionOverride(req.getSectionDescriptionOverride());
        if (req.getPortfolioSectionId() != null) {
            var ps = portfolioSections.findById(req.getPortfolioSectionId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid portfolioSectionId"));
            s.setPortfolioSection(ps);
        }

        List<UniversityApplicationPortfolioSection> list = ua.getPortfolioSections();
        if (list == null) list = new ArrayList<>();
        list.add(s);
        ua.setPortfolioSections(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/portfolio-sections/{sectionId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioSectionsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePortfolioSection(
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @Valid @RequestBody UniversityApplicationPortfolioSectionUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        s.setRequired(req.getRequired());
        s.setSortOrder(req.getSortOrder());
        s.setSectionNameOverride(req.getSectionNameOverride());
        s.setSectionDescriptionOverride(req.getSectionDescriptionOverride());
        if (req.getPortfolioSectionId() != null) {
            var ps = portfolioSections.findById(req.getPortfolioSectionId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid portfolioSectionId"));
            s.setPortfolioSection(ps);
        } else {
            s.setPortfolioSection(null);
        }
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/portfolio-sections/{sectionId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioSectionsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePortfolioSection(
        @PathVariable UUID id,
        @PathVariable UUID sectionId
    ) {
        UniversityApplication ua = getAny(id);
        removeByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/portfolio-sections/{sectionId}/files")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioFilesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPortfolioFile(
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @Valid @RequestBody UniversityApplicationPortfolioFileUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        UniversityApplicationPortfolioFile f = new UniversityApplicationPortfolioFile();
        f.setPortfolioSection(s);
        f.setType(req.getType());
        f.setName(req.getName());
        f.setDescription(req.getDescription());
        f.setFileUrl(req.getFileUrl());

        List<UniversityApplicationPortfolioFile> files = s.getFiles();
        if (files == null) files = new ArrayList<>();
        files.add(f);
        s.setFiles(files);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PatchMapping("/{id}/portfolio-sections/{sectionId}/files/{fileId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioFilesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePortfolioFile(
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @PathVariable UUID fileId,
        @Valid @RequestBody UniversityApplicationPortfolioFileUpsertRequestDto req
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        UniversityApplicationPortfolioFile f = findByIdOrThrow(s.getFiles(), fileId, "Portfolio file not found");
        f.setType(req.getType());
        f.setName(req.getName());
        f.setDescription(req.getDescription());
        f.setFileUrl(req.getFileUrl());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/portfolio-sections/{sectionId}/files/{fileId}")
    @Transactional
    @Operation(operationId = "adminUniversityApplicationsPortfolioFilesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePortfolioFile(
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @PathVariable UUID fileId
    ) {
        UniversityApplication ua = getAny(id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        removeByIdOrThrow(s.getFiles(), fileId, "Portfolio file not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    /**
     * Yeni portal kullanıcısı için: başvuru anındaki kimlik bilgileriyle tek bir {@link ApplicantProfile} oluşturulur.
     * Mevcut kullanıcı ile başvuru açılırken profil oluşturulmaz / güncellenmez.
     */
    private void createApplicantProfileForNewUserIfNeeded(UserAccount user, UniversityApplicationUpdateRequestDto snap) {
        if (snap == null) {
            return;
        }
        String fn = snap.getFirstName();
        String ln = snap.getLastName();
        if (fn == null || fn.isBlank() || ln == null || ln.isBlank()) {
            return;
        }
        if (applicantProfiles.findByUser_Id(user.getId()).isPresent()) {
            return;
        }
        ApplicantProfile ap = new ApplicantProfile();
        ap.setUser(user);
        ap.setFirstName(fn.trim());
        ap.setLastName(ln.trim());
        if (snap.getBirthDate() != null) {
            ap.setBirthDate(snap.getBirthDate());
        }
        if (snap.getPhone() != null && !snap.getPhone().isBlank()) {
            ap.setPhone(snap.getPhone().trim());
        }
        if (snap.getNationality() != null && !snap.getNationality().isBlank()) {
            ap.setNationality(snap.getNationality().trim());
        }
        if (snap.getAddress() != null && !snap.getAddress().isBlank()) {
            Address addr = new Address();
            addr.setLine1(snap.getAddress().trim());
            ap.setAddress(addr);
        }
        applicantProfiles.save(ap);
    }

    private UniversityApplication getAny(UUID id) {
        return applications.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private static List<String> ensureMutableList(List<String> current) {
        if (current == null) return new ArrayList<>();
        return new ArrayList<>(current);
    }

    private static void ensureIndex(List<?> list, int index) {
        if (index < 0 || index >= list.size()) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid index");
        }
    }

    private static <T extends com.genixo.ges.common.jpa.BaseEntity> T findByIdOrThrow(
        List<T> list,
        UUID id,
        String notFoundMessage
    ) {
        if (list == null || list.isEmpty()) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, notFoundMessage);
        }
        return list.stream()
            .filter(x -> x.getId() != null && x.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, notFoundMessage));
    }

    private static <T extends com.genixo.ges.common.jpa.BaseEntity> void removeByIdOrThrow(
        List<T> list,
        UUID id,
        String notFoundMessage
    ) {
        if (list == null || list.isEmpty()) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, notFoundMessage);
        }
        boolean removed = list.removeIf(x -> x.getId() != null && x.getId().equals(id));
        if (!removed) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, notFoundMessage);
        }
    }

    private static void applyUniversityApplicationPatch(UniversityApplication ua, UniversityApplicationUpdateRequestDto req) {
        if (req == null) {
            return;
        }
        if (req.getFirstName() != null) ua.setFirstName(req.getFirstName());
        if (req.getLastName() != null) ua.setLastName(req.getLastName());
        if (req.getBirthDate() != null) ua.setBirthDate(req.getBirthDate());
        if (req.getPhone() != null) ua.setPhone(req.getPhone());
        if (req.getNationality() != null) ua.setNationality(req.getNationality());
        if (req.getAddress() != null) ua.setAddress(req.getAddress());
        if (req.getCurrentSchool() != null) ua.setCurrentSchool(req.getCurrentSchool());
        if (req.getStudent() != null) ua.setStudent(req.getStudent());
        if (req.getClassLevel() != null) ua.setClassLevel(req.getClassLevel());
        if (req.getReferencePerson() != null) ua.setReferencePerson(req.getReferencePerson());
        if (req.getConsultancy() != null) ua.setConsultancy(req.getConsultancy());
        if (req.getFollowerPerson() != null) ua.setFollowerPerson(req.getFollowerPerson());

        if (req.getEducationLevel() != null) ua.setEducationLevel(req.getEducationLevel());
        if (req.getStartTermSeason() != null) ua.setStartTermSeason(req.getStartTermSeason());
        if (req.getStartYear() != null) ua.setStartYear(req.getStartYear());
        if (req.getYearlyBudgetMin() != null) ua.setYearlyBudgetMin(req.getYearlyBudgetMin());
        if (req.getYearlyBudgetMax() != null) ua.setYearlyBudgetMax(req.getYearlyBudgetMax());
        if (req.getScholarshipRequested() != null) ua.setScholarshipRequested(req.getScholarshipRequested());
        if (req.getScholarshipType() != null) ua.setScholarshipType(req.getScholarshipType());
        if (req.getAccommodationType() != null) ua.setAccommodationType(req.getAccommodationType());
        if (req.getPriceAmount() != null) ua.setPriceAmount(req.getPriceAmount());
        if (req.getPriceCurrency() != null) ua.setPriceCurrency(req.getPriceCurrency());
        if (req.getNotes() != null) ua.setNotes(req.getNotes());
    }

    private static UniversityApplicationByStatusListItemDto toByStatusListItemDto(UniversityApplication ua) {
        return UniversityApplicationByStatusListItemDto.builder()
            .id(ua.getId())
            .applicantFirstName(ua.getFirstName() != null ? ua.getFirstName()
                : (ua.getApplicantProfile() != null ? ua.getApplicantProfile().getFirstName() : null))
            .applicantLastName(ua.getLastName() != null ? ua.getLastName()
                : (ua.getApplicantProfile() != null ? ua.getApplicantProfile().getLastName() : null))
            .followerPerson(ua.getFollowerPerson())
            .status(ua.getStatus())
            .build();
    }

    private static PendingTaskListItemDto toPendingTaskListItemDto(
        UniversityApplication ua,
        UniversityApplicationTask t
    ) {
        return PendingTaskListItemDto.builder()
            .applicationId(ua.getId())
            .applicantFirstName(ua.getFirstName() != null ? ua.getFirstName()
                : (ua.getApplicantProfile() != null ? ua.getApplicantProfile().getFirstName() : null))
            .applicantLastName(ua.getLastName() != null ? ua.getLastName()
                : (ua.getApplicantProfile() != null ? ua.getApplicantProfile().getLastName() : null))
            .followerPerson(ua.getFollowerPerson())
            .taskId(t == null ? null : t.getId())
            .scheduledAt(t == null ? null : t.getScheduledAt())
            .withWhom(t == null ? null : t.getWithWhom())
            .whatToDo(t == null ? null : t.getWhatToDo())
            .taskCreatedAt(t == null ? null : t.getCreatedAt())
            .taskUpdatedAt(t == null ? null : t.getUpdatedAt())
            .build();
    }

    private UniversityApplicationListItemDto toListItemDto(UniversityApplication ua) {
        List<UniversityApplicationTask> tasks = ua.getTasks() == null ? List.of() : ua.getTasks();
        List<UniversityApplicationMeeting> meetings = ua.getMeetings() == null ? List.of() : ua.getMeetings();
        List<UniversityApplicationDocument> documents = ua.getDocuments() == null ? List.of() : ua.getDocuments();
        List<UniversityApplicationPayment> payments = ua.getPayments() == null ? List.of() : ua.getPayments();

        List<Instant> pendingTaskScheduledAts = tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
            .map(UniversityApplicationTask::getScheduledAt)
            .filter(d -> d != null)
            .sorted(Comparator.naturalOrder())
            .toList();
        int pendingTaskCount = (int) tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
            .count();
        int completedTaskCount = (int) tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.DONE)
            .count();

        int documentsWithFileCount = (int) documents.stream()
            .filter(d -> d.getDocumentUrl() != null && !d.getDocumentUrl().isBlank())
            .count();

        BigDecimal totalPaidAmount = payments.stream()
            .map(UniversityApplicationPayment::getAmount)
            .filter(a -> a != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return UniversityApplicationListItemDto.builder()
            .id(ua.getId())
            .firstName(ua.getFirstName() != null ? ua.getFirstName() : (ua.getApplicantProfile() == null ? null : ua.getApplicantProfile().getFirstName()))
            .lastName(ua.getLastName() != null ? ua.getLastName() : (ua.getApplicantProfile() == null ? null : ua.getApplicantProfile().getLastName()))
            .status(ua.getStatus())
            .educationLevel(ua.getEducationLevel())
            .followerPerson(ua.getFollowerPerson())
            .priceAmount(ua.getPriceAmount())
            .priceCurrency(ua.getPriceCurrency())
            .totalPaidAmount(totalPaidAmount)
            .pendingTaskCount(pendingTaskCount)
            .completedTaskCount(completedTaskCount)
            .pendingTaskScheduledAts(pendingTaskScheduledAts)
            .meetingCount(meetings.size())
            .documentCount(documents.size())
            .documentsWithFileCount(documentsWithFileCount)
            .createdAt(ua.getCreatedAt())
            .updatedAt(ua.getUpdatedAt())
            .build();
    }

    private UniversityApplicationDetailDto toDetailDto(UniversityApplication ua) {
        return UniversityApplicationDetailDto.builder()
            .id(ua.getId())
            .applicantUserId(ua.getApplicant() == null ? null : ua.getApplicant().getId())
            .applicantEmail(ua.getApplicant() == null ? null : ua.getApplicant().getEmail())
            .status(ua.getStatus())
            .educationLevel(ua.getEducationLevel())
            .firstName(ua.getFirstName())
            .lastName(ua.getLastName())
            .birthDate(ua.getBirthDate())
            .phone(ua.getPhone())
            .nationality(ua.getNationality())
            .address(ua.getAddress())
            .currentSchool(ua.getCurrentSchool())
            .student(ua.getStudent())
            .classLevel(ua.getClassLevel())
            .referencePerson(ua.getReferencePerson())
            .consultancy(ua.getConsultancy())
            .followerPerson(ua.getFollowerPerson())
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
            .priceAmount(ua.getPriceAmount())
            .priceCurrency(ua.getPriceCurrency())
            .notes(ua.getNotes())
            .preferencesCompletedAt(ua.getPreferencesCompletedAt())
            .applicationNotes(ua.getApplicationNotes() == null ? List.of() : ua.getApplicationNotes().stream()
                .map(n -> com.genixo.ges.api.university.dto.UniversityApplicationNoteDto.builder()
                    .id(n.getId())
                    .writtenBy(n.getWrittenBy())
                    .writtenAt(n.getWrittenAt())
                    .todoText(n.getTodoText())
                    .createdAt(n.getCreatedAt())
                    .updatedAt(n.getUpdatedAt())
                    .build())
                .toList())
            .meetings(ua.getMeetings() == null ? List.of() : ua.getMeetings().stream()
                .map(m -> com.genixo.ges.api.university.dto.UniversityApplicationMeetingDto.builder()
                    .id(m.getId())
                    .person(m.getPerson())
                    .meetingAt(m.getMeetingAt())
                    .meetingNote(m.getMeetingNote())
                    .meetingResult(m.getMeetingResult())
                    .createdAt(m.getCreatedAt())
                    .updatedAt(m.getUpdatedAt())
                    .build())
                .toList())
            .tasks(ua.getTasks() == null ? List.of() : ua.getTasks().stream()
                .map(t -> com.genixo.ges.api.university.dto.UniversityApplicationTaskDto.builder()
                    .id(t.getId())
                    .scheduledAt(t.getScheduledAt())
                    .withWhom(t.getWithWhom())
                    .whatToDo(t.getWhatToDo())
                    .status(t.getStatus())
                    .performedByUser(t.getPerformedByUser())
                    .createdAt(t.getCreatedAt())
                    .updatedAt(t.getUpdatedAt())
                    .build())
                .toList())
            .documents(ua.getDocuments() == null ? List.of() : ua.getDocuments().stream()
                .map(d -> com.genixo.ges.api.university.dto.UniversityApplicationDocumentDto.builder()
                    .id(d.getId())
                    .required(d.getRequired())
                    .documentName(d.getDocumentName())
                    .documentDescription(d.getDocumentDescription())
                    .documentUrl(d.getDocumentUrl())
                    .uploadedAt(d.getUploadedAt())
                    .createdAt(d.getCreatedAt())
                    .updatedAt(d.getUpdatedAt())
                    .build())
                .toList())
            .portfolioSections(ua.getPortfolioSections() == null ? List.of() : ua.getPortfolioSections().stream()
                .map(s -> com.genixo.ges.api.university.dto.UniversityApplicationPortfolioSectionDto.builder()
                    .id(s.getId())
                    .required(s.getRequired())
                    .sortOrder(s.getSortOrder())
                    .portfolioSectionId(s.getPortfolioSection() == null ? null : s.getPortfolioSection().getId())
                    .portfolioSection(PortfolioSectionMapper.toDto(s.getPortfolioSection()))
                    .sectionNameOverride(s.getSectionNameOverride())
                    .sectionDescriptionOverride(s.getSectionDescriptionOverride())
                    .files(s.getFiles() == null ? List.of() : s.getFiles().stream()
                        .map(f -> com.genixo.ges.api.university.dto.UniversityApplicationPortfolioFileDto.builder()
                            .id(f.getId())
                            .type(f.getType())
                            .name(f.getName())
                            .description(f.getDescription())
                            .fileUrl(f.getFileUrl())
                            .createdAt(f.getCreatedAt())
                            .updatedAt(f.getUpdatedAt())
                            .build())
                        .toList())
                    .createdAt(s.getCreatedAt())
                    .updatedAt(s.getUpdatedAt())
                    .build())
                .toList())
            .payments(ua.getPayments() == null ? List.of() : ua.getPayments().stream()
                .map(p -> com.genixo.ges.api.university.dto.UniversityApplicationPaymentDto.builder()
                    .id(p.getId())
                    .paymentAt(p.getPaymentAt())
                    .amount(p.getAmount())
                    .currency(p.getCurrency())
                    .receivedBy(p.getReceivedBy())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build())
                .toList())
            .createdAt(ua.getCreatedAt())
            .updatedAt(ua.getUpdatedAt())
            .build();
    }
}


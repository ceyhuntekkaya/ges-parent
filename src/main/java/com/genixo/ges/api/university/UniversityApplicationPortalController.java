package com.genixo.ges.api.university;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.university.dto.PortfolioSectionDto;
import com.genixo.ges.api.university.dto.UniversityApplicationCreateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDetailDto;
import com.genixo.ges.api.university.dto.UniversityApplicationDocumentUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationMeetingUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationNoteCreateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationNoteUpdateRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPaymentUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPortfolioFileUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationPortfolioSectionUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationTaskUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationStringListItemUpsertRequestDto;
import com.genixo.ges.api.university.dto.UniversityApplicationUpdateRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.university.model.PortfolioSection;
import com.genixo.ges.university.model.UniversityApplicationDocument;
import com.genixo.ges.university.model.UniversityApplicationMeeting;
import com.genixo.ges.university.model.UniversityApplicationNote;
import com.genixo.ges.university.model.UniversityApplicationPayment;
import com.genixo.ges.university.model.UniversityApplicationPortfolioFile;
import com.genixo.ges.university.model.UniversityApplicationPortfolioSection;
import com.genixo.ges.university.model.UniversityApplicationTask;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import com.genixo.ges.storage.FileStorageService;
import com.genixo.ges.storage.model.StoredFile;
import com.genixo.ges.university.repo.PortfolioSectionRepository;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/v1/portal/university-applications")
public class UniversityApplicationPortalController {

    private final UniversityApplicationRepository applications;
    private final UserAccountRepository users;
    private final PortfolioSectionRepository portfolioSections;
    private final UniversityApplicationDocumentSeeder documentSeeder;
    private final UniversityApplicationPortfolioSeeder portfolioSeeder;
    private final PortalUniversityApplicationFileService applicationFiles;
    private final FileStorageService storage;

    public UniversityApplicationPortalController(
        UniversityApplicationRepository applications,
        UserAccountRepository users,
        PortfolioSectionRepository portfolioSections,
        UniversityApplicationDocumentSeeder documentSeeder,
        UniversityApplicationPortfolioSeeder portfolioSeeder,
        PortalUniversityApplicationFileService applicationFiles,
        FileStorageService storage
    ) {
        this.applications = applications;
        this.users = users;
        this.portfolioSections = portfolioSections;
        this.documentSeeder = documentSeeder;
        this.portfolioSeeder = portfolioSeeder;
        this.applicationFiles = applicationFiles;
        this.storage = storage;
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsCreateDraft")
    public ResponseEntity<UniversityApplicationDetailDto> createDraft(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @Valid @RequestBody UniversityApplicationCreateRequestDto req
    ) {
        if ("USER".equals(principal.getRole())) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "University applications cannot be created by portal users");
        }

        UserAccount applicant = users.findById(principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "User not found"));

        UniversityApplication ua = new UniversityApplication();
        ua.setApplicant(applicant);
        ua.setEducationLevel(req.getEducationLevel());
        ua.setStatus(ApplicationStatus.DRAFT);
        documentSeeder.seedFromActiveRequirements(ua);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(ua));
    }

    @GetMapping
    @Operation(operationId = "portalUniversityApplicationsListMine")
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
    @Transactional(readOnly = true)
    @Operation(operationId = "portalUniversityApplicationsGetMine")
    public ResponseEntity<UniversityApplicationDetailDto> getMine(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id
    ) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @GetMapping("/{id}/files/{storedFileId}/download")
    @Transactional(readOnly = true)
    @Operation(operationId = "portalUniversityApplicationsDownloadFile")
    public ResponseEntity<Resource> downloadFile(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID storedFileId
    ) {
        StoredFile sf = applicationFiles.resolveForApplicant(id, storedFileId, principal.getId());
        var path = storage.resolvePath(sf);
        if (!Files.exists(path)) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "File not found on disk");
        }

        Resource res = new FileSystemResource(path);
        String ct = sf.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : sf.getContentType();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sf.getOriginalFilename().replace("\"", "") + "\"")
            .contentType(MediaType.parseMediaType(ct))
            .contentLength(sf.getSizeBytes())
            .body(res);
    }

    @PatchMapping("/{id}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsUpdateDraft")
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

        if (req.getEducationLevel() != null) {
            portfolioSeeder.seedMatchingTemplates(ua);
        }

        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/department-preferences")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsDepartmentPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addDepartmentPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        list.add(req.getValue());
        ua.setDepartmentPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/department-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsDepartmentPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateDepartmentPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setDepartmentPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/department-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsDepartmentPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteDepartmentPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getDepartmentPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setDepartmentPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        portfolioSeeder.seedMatchingTemplates(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/country-preferences")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsCountryPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addCountryPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        list.add(req.getValue());
        ua.setCountryPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/country-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsCountryPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateCountryPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setCountryPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/country-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsCountryPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteCountryPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getCountryPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setCountryPreferences(list);
        markPreferencesCompletedIfNeeded(ua);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/university-preferences")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsUniversityPreferencesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addUniversityPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        list.add(req.getValue());
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PutMapping("/{id}/university-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsUniversityPreferencesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateUniversityPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index,
        @Valid @RequestBody UniversityApplicationStringListItemUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        ensureIndex(list, index);
        list.set(index, req.getValue());
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/university-preferences/{index}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsUniversityPreferencesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteUniversityPreference(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable int index
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        List<String> list = ensureMutableList(ua.getUniversityPreferences());
        ensureIndex(list, index);
        list.remove(index);
        ua.setUniversityPreferences(list);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/notes")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsNotesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addNote(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationNoteCreateRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationNote n = new UniversityApplicationNote();
        n.setApplication(ua);
        n.setWrittenBy(principal.getEmail());
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
    @Operation(operationId = "portalUniversityApplicationsNotesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateNote(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID noteId,
        @Valid @RequestBody UniversityApplicationNoteUpdateRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationNote n = findByIdOrThrow(ua.getApplicationNotes(), noteId, "Note not found");
        n.setTodoText(req.getTodoText());
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/notes/{noteId}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsNotesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteNote(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID noteId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getApplicationNotes(), noteId, "Note not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/meetings")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsMeetingsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addMeeting(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationMeetingUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsMeetingsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateMeeting(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID meetingId,
        @Valid @RequestBody UniversityApplicationMeetingUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsMeetingsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteMeeting(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID meetingId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getMeetings(), meetingId, "Meeting not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/tasks")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsTasksAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationTaskUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationTask t = new UniversityApplicationTask();
        t.setApplication(ua);
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        t.setStatus(req.getStatus() == null ? UniversityApplicationTaskStatus.PENDING : req.getStatus());
        if (t.getStatus() == UniversityApplicationTaskStatus.DONE) {
            t.setPerformedByUser(principal.getEmail());
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
    @Operation(operationId = "portalUniversityApplicationsTasksUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID taskId,
        @Valid @RequestBody UniversityApplicationTaskUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationTask t = findByIdOrThrow(ua.getTasks(), taskId, "Task not found");
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
            if (req.getStatus() == UniversityApplicationTaskStatus.DONE && (t.getPerformedByUser() == null || t.getPerformedByUser().isBlank())) {
                t.setPerformedByUser(principal.getEmail());
            }
        }
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsTasksDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID taskId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getTasks(), taskId, "Task not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/documents")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsDocumentsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addDocument(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationDocumentUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsDocumentsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updateDocument(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID documentId,
        @Valid @RequestBody UniversityApplicationDocumentUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationDocument d = findByIdOrThrow(ua.getDocuments(), documentId, "Document not found");
        UniversityApplicationDocumentMutations.applyForUpdate(d, req);
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsDocumentsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deleteDocument(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID documentId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getDocuments(), documentId, "Document not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/payments")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsPaymentsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPayment(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationPaymentUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsPaymentsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePayment(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID paymentId,
        @Valid @RequestBody UniversityApplicationPaymentUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsPaymentsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePayment(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID paymentId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getPayments(), paymentId, "Payment not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/portfolio-sections")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsPortfolioSectionsAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPortfolioSection(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody UniversityApplicationPortfolioSectionUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);

        UniversityApplicationPortfolioSection s = new UniversityApplicationPortfolioSection();
        s.setApplication(ua);
        s.setRequired(req.getRequired());
        s.setSortOrder(req.getSortOrder());
        s.setSectionNameOverride(req.getSectionNameOverride());
        s.setSectionDescriptionOverride(req.getSectionDescriptionOverride());
        if (req.getPortfolioSectionId() != null) {
            PortfolioSection ps = portfolioSections.findById(req.getPortfolioSectionId())
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
    @Operation(operationId = "portalUniversityApplicationsPortfolioSectionsUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePortfolioSection(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @Valid @RequestBody UniversityApplicationPortfolioSectionUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        s.setRequired(req.getRequired());
        s.setSortOrder(req.getSortOrder());
        s.setSectionNameOverride(req.getSectionNameOverride());
        s.setSectionDescriptionOverride(req.getSectionDescriptionOverride());
        if (req.getPortfolioSectionId() != null) {
            PortfolioSection ps = portfolioSections.findById(req.getPortfolioSectionId())
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
    @Operation(operationId = "portalUniversityApplicationsPortfolioSectionsDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePortfolioSection(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID sectionId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        removeByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/portfolio-sections/{sectionId}/files")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsPortfolioFilesAdd")
    public ResponseEntity<UniversityApplicationDetailDto> addPortfolioFile(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @Valid @RequestBody UniversityApplicationPortfolioFileUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsPortfolioFilesUpdate")
    public ResponseEntity<UniversityApplicationDetailDto> updatePortfolioFile(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @PathVariable UUID fileId,
        @Valid @RequestBody UniversityApplicationPortfolioFileUpsertRequestDto req
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
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
    @Operation(operationId = "portalUniversityApplicationsPortfolioFilesDelete")
    public ResponseEntity<UniversityApplicationDetailDto> deletePortfolioFile(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID sectionId,
        @PathVariable UUID fileId
    ) {
        UniversityApplication ua = getDraftMine(principal, id);
        UniversityApplicationPortfolioSection s = findByIdOrThrow(ua.getPortfolioSections(), sectionId, "Portfolio section not found");
        removeByIdOrThrow(s.getFiles(), fileId, "Portfolio file not found");
        applications.save(ua);
        return ResponseEntity.ok(toDetailDto(ua));
    }

    @PostMapping("/{id}/submit")
    @Transactional
    @Operation(operationId = "portalUniversityApplicationsSubmit")
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

    private UniversityApplication getDraftMine(AuthUserPrincipal principal, UUID id) {
        UniversityApplication ua = applications.findByIdAndApplicant_Id(id, principal.getId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        if (ua.getStatus() != ApplicationStatus.DRAFT) {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Only DRAFT applications can be updated");
        }
        return ua;
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

    private static void markPreferencesCompletedIfNeeded(UniversityApplication ua) {
        if (ua.getPreferencesCompletedAt() != null) return;
        if (ua.getDepartmentPreferences() != null && !ua.getDepartmentPreferences().isEmpty()
            && ua.getCountryPreferences() != null && !ua.getCountryPreferences().isEmpty()) {
            ua.setPreferencesCompletedAt(Instant.now());
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


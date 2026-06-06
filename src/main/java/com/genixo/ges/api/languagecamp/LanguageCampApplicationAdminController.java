package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationAdminUpdateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDocumentUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationMeetingUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationNoteCreateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationNoteUpdateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationPaymentUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationTaskUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampPaymentCompletedRequestDto;
import com.genixo.ges.api.university.dto.ApplicationStatusChangeRequestDto;
import com.genixo.ges.api.university.dto.PendingTaskListItemDto;
import com.genixo.ges.api.university.dto.UniversityApplicationByStatusListItemDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationDocument;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationMeeting;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationNote;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationPayment;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationTask;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/v1/admin/language-camp-applications")
public class LanguageCampApplicationAdminController {

    private final LanguageCampApplicationRepository apps;
    private final LanguageCampApplicationGroupService groups;

    public LanguageCampApplicationAdminController(
        LanguageCampApplicationRepository apps,
        LanguageCampApplicationGroupService groups
    ) {
        this.apps = apps;
        this.groups = groups;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(operationId = "adminLanguageCampApplicationsList")
    public ResponseEntity<PageDto<LanguageCampApplicationListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) Boolean paymentCompleted,
        @RequestParam(required = false) UUID languageCampProjectId
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = apps.findAdminList(languageCampProjectId, status, paymentCompleted, pageable);
        var items = p.getContent().stream()
            .map(LanguageCampApplicationDtoMapper::toListItemDto)
            .toList();

        return ResponseEntity.ok(PageDto.<LanguageCampApplicationListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/pending-tasks")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminLanguageCampApplicationsPendingTasks")
    public ResponseEntity<List<PendingTaskListItemDto>> listPendingTasks() {
        List<PendingTaskListItemDto> items = apps
            .findAllWithTasksByTaskStatus(UniversityApplicationTaskStatus.PENDING)
            .stream()
            .flatMap(a -> a.getTasks().stream()
                .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
                .map(t -> toPendingTaskListItemDto(a, t)))
            .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/by-status")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminLanguageCampApplicationsByStatus")
    public ResponseEntity<List<UniversityApplicationByStatusListItemDto>> listByStatus(
        @RequestParam ApplicationStatus status
    ) {
        var items = apps.findAll().stream()
            .filter(a -> a.getStatus() == status)
            .map(this::toByStatusListItemDto)
            .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminLanguageCampApplicationsGet")
    public ResponseEntity<LanguageCampApplicationDetailDto> get(@PathVariable UUID id) {
        LanguageCampApplication a = getAny(id);
        return ResponseEntity.ok(groups.enrichDetail(a));
    }

    @PatchMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationAdminUpdateRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationDtoMapper.applyAdminPatch(a, req);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsChangeStatus")
    public ResponseEntity<LanguageCampApplicationDetailDto> changeStatus(
        @PathVariable UUID id,
        @RequestBody ApplicationStatusChangeRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        a.setStatus(req.getStatus());
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/payment-completed")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsSetPaymentCompleted")
    public ResponseEntity<LanguageCampApplicationDetailDto> setPaymentCompleted(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampPaymentCompletedRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        a.setPaymentCompleted(req.getPaymentCompleted());
        apps.save(a);
        return get(id);
    }

    @PostMapping("/{id}/notes")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsNotesAdd")
    public ResponseEntity<LanguageCampApplicationDetailDto> addNote(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationNoteCreateRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationNote n = new LanguageCampApplicationNote();
        n.setApplication(a);
        n.setWrittenBy(principal == null ? "admin" : principal.getEmail());
        n.setWrittenAt(Instant.now());
        n.setTodoText(req.getTodoText());

        List<LanguageCampApplicationNote> list = a.getApplicationNotes();
        if (list == null) list = new ArrayList<>();
        list.add(n);
        a.setApplicationNotes(list);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/notes/{noteId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsNotesUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> updateNote(
        @PathVariable UUID id,
        @PathVariable UUID noteId,
        @Valid @RequestBody LanguageCampApplicationNoteUpdateRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationNote n = findByIdOrThrow(a.getApplicationNotes(), noteId, "Note not found");
        n.setTodoText(req.getTodoText());
        apps.save(a);
        return get(id);
    }

    @DeleteMapping("/{id}/notes/{noteId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsNotesDelete")
    public ResponseEntity<LanguageCampApplicationDetailDto> deleteNote(
        @PathVariable UUID id,
        @PathVariable UUID noteId
    ) {
        LanguageCampApplication a = getAny(id);
        removeByIdOrThrow(a.getApplicationNotes(), noteId, "Note not found");
        apps.save(a);
        return get(id);
    }

    @PostMapping("/{id}/meetings")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsMeetingsAdd")
    public ResponseEntity<LanguageCampApplicationDetailDto> addMeeting(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationMeetingUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationMeeting m = new LanguageCampApplicationMeeting();
        m.setApplication(a);
        m.setPerson(req.getPerson());
        m.setMeetingAt(req.getMeetingAt());
        m.setMeetingNote(req.getMeetingNote());
        m.setMeetingResult(req.getMeetingResult());

        List<LanguageCampApplicationMeeting> list = a.getMeetings();
        if (list == null) list = new ArrayList<>();
        list.add(m);
        a.setMeetings(list);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/meetings/{meetingId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsMeetingsUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> updateMeeting(
        @PathVariable UUID id,
        @PathVariable UUID meetingId,
        @Valid @RequestBody LanguageCampApplicationMeetingUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationMeeting m = findByIdOrThrow(a.getMeetings(), meetingId, "Meeting not found");
        m.setPerson(req.getPerson());
        m.setMeetingAt(req.getMeetingAt());
        m.setMeetingNote(req.getMeetingNote());
        m.setMeetingResult(req.getMeetingResult());
        apps.save(a);
        return get(id);
    }

    @DeleteMapping("/{id}/meetings/{meetingId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsMeetingsDelete")
    public ResponseEntity<LanguageCampApplicationDetailDto> deleteMeeting(
        @PathVariable UUID id,
        @PathVariable UUID meetingId
    ) {
        LanguageCampApplication a = getAny(id);
        removeByIdOrThrow(a.getMeetings(), meetingId, "Meeting not found");
        apps.save(a);
        return get(id);
    }

    @PostMapping("/{id}/tasks")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsTasksAdd")
    public ResponseEntity<LanguageCampApplicationDetailDto> addTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationTaskUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationTask t = new LanguageCampApplicationTask();
        t.setApplication(a);
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        t.setStatus(req.getStatus() == null ? UniversityApplicationTaskStatus.PENDING : req.getStatus());
        if (t.getStatus() == UniversityApplicationTaskStatus.DONE) {
            t.setPerformedByUser(principal == null ? "admin" : principal.getEmail());
        }

        List<LanguageCampApplicationTask> list = a.getTasks();
        if (list == null) list = new ArrayList<>();
        list.add(t);
        a.setTasks(list);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/tasks/{taskId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsTasksUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> updateTask(
        @AuthenticationPrincipal AuthUserPrincipal principal,
        @PathVariable UUID id,
        @PathVariable UUID taskId,
        @Valid @RequestBody LanguageCampApplicationTaskUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationTask t = findByIdOrThrow(a.getTasks(), taskId, "Task not found");
        t.setScheduledAt(req.getScheduledAt());
        t.setWithWhom(req.getWithWhom());
        t.setWhatToDo(req.getWhatToDo());
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
            if (req.getStatus() == UniversityApplicationTaskStatus.DONE
                && (t.getPerformedByUser() == null || t.getPerformedByUser().isBlank())) {
                t.setPerformedByUser(principal == null ? "admin" : principal.getEmail());
            }
        }
        apps.save(a);
        return get(id);
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsTasksDelete")
    public ResponseEntity<LanguageCampApplicationDetailDto> deleteTask(
        @PathVariable UUID id,
        @PathVariable UUID taskId
    ) {
        LanguageCampApplication a = getAny(id);
        removeByIdOrThrow(a.getTasks(), taskId, "Task not found");
        apps.save(a);
        return get(id);
    }

    @PostMapping("/{id}/documents")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsDocumentsAdd")
    public ResponseEntity<LanguageCampApplicationDetailDto> addDocument(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationDocumentUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationDocument d = new LanguageCampApplicationDocument();
        d.setApplication(a);
        LanguageCampApplicationDocumentMutations.applyForCreate(d, req);

        List<LanguageCampApplicationDocument> list = a.getDocuments();
        if (list == null) list = new ArrayList<>();
        list.add(d);
        a.setDocuments(list);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsDocumentsUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> updateDocument(
        @PathVariable UUID id,
        @PathVariable UUID documentId,
        @Valid @RequestBody LanguageCampApplicationDocumentUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationDocument d = findByIdOrThrow(a.getDocuments(), documentId, "Document not found");
        LanguageCampApplicationDocumentMutations.applyForUpdate(d, req);
        apps.save(a);
        return get(id);
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsDocumentsDelete")
    public ResponseEntity<LanguageCampApplicationDetailDto> deleteDocument(
        @PathVariable UUID id,
        @PathVariable UUID documentId
    ) {
        LanguageCampApplication a = getAny(id);
        removeByIdOrThrow(a.getDocuments(), documentId, "Document not found");
        apps.save(a);
        return get(id);
    }

    @PostMapping("/{id}/payments")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsPaymentsAdd")
    public ResponseEntity<LanguageCampApplicationDetailDto> addPayment(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampApplicationPaymentUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationPayment p = new LanguageCampApplicationPayment();
        p.setApplication(a);
        p.setPaymentAt(req.getPaymentAt());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setReceivedBy(req.getReceivedBy());

        List<LanguageCampApplicationPayment> list = a.getPayments();
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(p);
        a.setPayments(list);
        apps.save(a);
        return get(id);
    }

    @PatchMapping("/{id}/payments/{paymentId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsPaymentsUpdate")
    public ResponseEntity<LanguageCampApplicationDetailDto> updatePayment(
        @PathVariable UUID id,
        @PathVariable UUID paymentId,
        @Valid @RequestBody LanguageCampApplicationPaymentUpsertRequestDto req
    ) {
        LanguageCampApplication a = getAny(id);
        LanguageCampApplicationPayment p = findByIdOrThrow(a.getPayments(), paymentId, "Payment not found");
        p.setPaymentAt(req.getPaymentAt());
        p.setAmount(req.getAmount());
        p.setCurrency(req.getCurrency());
        p.setReceivedBy(req.getReceivedBy());
        apps.save(a);
        return get(id);
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    @Transactional
    @Operation(operationId = "adminLanguageCampApplicationsPaymentsDelete")
    public ResponseEntity<LanguageCampApplicationDetailDto> deletePayment(
        @PathVariable UUID id,
        @PathVariable UUID paymentId
    ) {
        LanguageCampApplication a = getAny(id);
        removeByIdOrThrow(a.getPayments(), paymentId, "Payment not found");
        apps.save(a);
        return get(id);
    }

    private LanguageCampApplication getAny(UUID id) {
        return apps.findByIdWithApplicant(id)
            .or(() -> apps.findById(id))
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private static PendingTaskListItemDto toPendingTaskListItemDto(
        LanguageCampApplication a,
        LanguageCampApplicationTask t
    ) {
        return PendingTaskListItemDto.builder()
            .applicationId(a.getId())
            .applicantFirstName(a.getFirstName())
            .applicantLastName(a.getLastName())
            .followerPerson(a.getFollowerPerson())
            .taskId(t == null ? null : t.getId())
            .scheduledAt(t == null ? null : t.getScheduledAt())
            .withWhom(t == null ? null : t.getWithWhom())
            .whatToDo(t == null ? null : t.getWhatToDo())
            .taskCreatedAt(t == null ? null : t.getCreatedAt())
            .taskUpdatedAt(t == null ? null : t.getUpdatedAt())
            .build();
    }

    private UniversityApplicationByStatusListItemDto toByStatusListItemDto(LanguageCampApplication a) {
        return UniversityApplicationByStatusListItemDto.builder()
            .id(a.getId())
            .applicantFirstName(a.getFirstName())
            .applicantLastName(a.getLastName())
            .followerPerson(a.getFollowerPerson())
            .status(a.getStatus())
            .build();
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
}

package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationPaymentUpsertRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampPaymentCompletedRequestDto;
import com.genixo.ges.api.university.dto.ApplicationStatusChangeRequestDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationPayment;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public LanguageCampApplicationAdminController(LanguageCampApplicationRepository apps) {
        this.apps = apps;
    }

    @GetMapping
    @Operation(operationId = "adminLanguageCampApplicationsList")
    public ResponseEntity<PageDto<LanguageCampApplicationListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) Boolean paymentCompleted
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = apps.findAll(pageable);
        var items = p.getContent().stream()
            .filter(a -> status == null || a.getStatus() == status)
            .filter(a -> paymentCompleted == null || a.isPaymentCompleted() == paymentCompleted)
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

    @GetMapping("/{id}")
    @Operation(operationId = "adminLanguageCampApplicationsGet")
    public ResponseEntity<LanguageCampApplicationDetailDto> get(@PathVariable UUID id) {
        LanguageCampApplication a = apps.findDetailById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
        return ResponseEntity.ok(LanguageCampApplicationDtoMapper.toDetailDto(a));
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
        return apps.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Application not found"));
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

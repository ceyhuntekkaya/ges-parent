package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationGroupListItemDto;
import com.genixo.ges.application.model.ApplicationStatus;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/language-camp-application-groups")
public class LanguageCampApplicationGroupAdminController {

    private final LanguageCampApplicationGroupService groups;

    public LanguageCampApplicationGroupAdminController(LanguageCampApplicationGroupService groups) {
        this.groups = groups;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(operationId = "adminLanguageCampApplicationGroupsList")
    public ResponseEntity<PageDto<LanguageCampApplicationGroupListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) Boolean paymentCompleted,
        @RequestParam(required = false) UUID languageCampProjectId
    ) {
        return ResponseEntity.ok(groups.listAdminGroups(page, size, languageCampProjectId, status, paymentCompleted));
    }
}

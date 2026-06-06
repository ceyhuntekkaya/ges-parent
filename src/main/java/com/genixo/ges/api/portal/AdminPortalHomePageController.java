package com.genixo.ges.api.portal;

import com.genixo.ges.api.portal.dto.PortalHomePageContentDto;
import com.genixo.ges.api.portal.dto.PortalHomePageContentUpdateRequestDto;
import com.genixo.ges.portal.PortalHomePageContentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/home-page")
public class AdminPortalHomePageController {

    private final PortalHomePageContentService homePage;

    public AdminPortalHomePageController(PortalHomePageContentService homePage) {
        this.homePage = homePage;
    }

    @GetMapping
    @Operation(operationId = "adminHomePageGet")
    public ResponseEntity<PortalHomePageContentDto> get() {
        return ResponseEntity.ok(homePage.getSingleton());
    }

    @PutMapping
    @Operation(operationId = "adminHomePageUpdate")
    public ResponseEntity<PortalHomePageContentDto> update(@Valid @RequestBody PortalHomePageContentUpdateRequestDto req) {
        return ResponseEntity.ok(homePage.update(req));
    }
}

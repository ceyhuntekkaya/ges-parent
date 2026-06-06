package com.genixo.ges.api.portal;

import com.genixo.ges.api.portal.dto.PortalHomePageContentDto;
import com.genixo.ges.portal.PortalHomePageContentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/public/home-page")
public class PortalHomePagePublicController {

    private final PortalHomePageContentService homePage;

    public PortalHomePagePublicController(PortalHomePageContentService homePage) {
        this.homePage = homePage;
    }

    @GetMapping
    @Operation(operationId = "publicHomePageGet")
    public ResponseEntity<PortalHomePageContentDto> get() {
        return ResponseEntity.ok(homePage.getSingleton());
    }
}

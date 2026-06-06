package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectPublicListItemDto;
import com.genixo.ges.company.repo.CompanyRepository;
import com.genixo.ges.languagecamp.model.EProjectStatus;
import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.repo.LanguageCampProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
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
@RequestMapping("/v1/public/language-camp-projects")
public class LanguageCampProjectPublicController {

    private final LanguageCampProjectRepository projects;
    private final CompanyRepository companies;
    private static final Pattern PORTAL_FILE_URL = Pattern.compile(".*/v1/portal/files/([0-9a-fA-F-]{36})/download.*");

    public LanguageCampProjectPublicController(
        LanguageCampProjectRepository projects,
        CompanyRepository companies
    ) {
        this.projects = projects;
        this.companies = companies;
    }

    @GetMapping
    @Operation(operationId = "publicLanguageCampProjectsListActive")
    public ResponseEntity<PageDto<LanguageCampProjectPublicListItemDto>> listActive(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "24") int size,
        @RequestParam(defaultValue = "true") boolean individual,
        @RequestParam(required = false) String companyCode
    ) {
        if (!individual && (companyCode == null || companyCode.isBlank())) {
            return ResponseEntity.ok(emptyPage(page, size));
        }

        String normalizedCompanyCode = null;
        if (!individual) {
            normalizedCompanyCode = companyCode.trim();
            companies.findByCode(normalizedCompanyCode)
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid companyCode"));
        }

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = individual
            ? projects.findOpenForApplication(
                EProjectStatus.ACTIVE,
                true,
                Instant.now(),
                null,
                pageable
            )
            : projects.findActiveCorporateByCompanyCode(
                EProjectStatus.ACTIVE,
                normalizedCompanyCode,
                pageable
            );
        var items = p.getContent().stream().map(this::toPublicListItemDto).toList();

        return ResponseEntity.ok(PageDto.<LanguageCampProjectPublicListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    private PageDto<LanguageCampProjectPublicListItemDto> emptyPage(int page, int size) {
        return PageDto.<LanguageCampProjectPublicListItemDto>builder()
            .items(List.of())
            .page(page)
            .size(size)
            .totalItems(0)
            .totalPages(0)
            .build();
    }

    @GetMapping("/{id}")
    @Operation(operationId = "publicLanguageCampProjectsGetActive")
    public ResponseEntity<LanguageCampProjectDetailDto> getActive(@PathVariable UUID id) {
        LanguageCampProject p = projects.findByIdAndProjectStatus(id, EProjectStatus.ACTIVE)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found"));
        return ResponseEntity.ok(toDetailDto(p));
    }

    private LanguageCampProjectPublicListItemDto toPublicListItemDto(LanguageCampProject p) {
        return LanguageCampProjectPublicListItemDto.builder()
            .id(p.getId())
            .title(p.getTitle())
            .projectType(p.getProjectType())
            .smallBanner(toPublicMediaUrl(p.getSmallBanner()))
            .location(p.getLocation())
            .duration(p.getDuration())
            .price(p.getPrice())
            .originalPrice(p.getOriginalPrice())
            .currency(p.getCurrency())
            .individual(Boolean.TRUE.equals(p.getIndividual()))
            .build();
    }

    private LanguageCampProjectDetailDto toDetailDto(LanguageCampProject p) {
        return LanguageCampProjectDetailDto.builder()
            .id(p.getId())
            .title(p.getTitle())
            .companyId(p.getCompany() == null ? null : p.getCompany().getId())
            .quota(p.getQuota())
            .applicationStartAt(p.getApplicationStartAt())
            .applicationEndAt(p.getApplicationEndAt())
            .projectStartAt(p.getProjectStartAt())
            .projectEndAt(p.getProjectEndAt())
            .projectStatus(p.getProjectStatus())
            .projectType(p.getProjectType())
            .banner(toPublicMediaUrl(p.getBanner()))
            .smallBanner(toPublicMediaUrl(p.getSmallBanner()))
            .images(p.getImages() == null ? null : p.getImages().stream().map(this::toPublicMediaUrl).toList())
            .presentationVideoUrl(p.getPresentationVideoUrl())
            .presentationDocumentUrl(p.getPresentationDocumentUrl())
            .description(p.getDescription())
            .duration(p.getDuration())
            .primaryLocations(p.getPrimaryLocations())
            .locations(p.getLocations())
            .location(p.getLocation())
            .price(p.getPrice())
            .originalPrice(p.getOriginalPrice())
            .currency(p.getCurrency())
            .included(p.getIncluded())
            .excluded(p.getExcluded())
            .highlights(p.getHighlights())
            .itinerary(p.getItinerary())
            .allowParent(p.getAllowParent())
            .allowTeacher(p.getAllowTeacher())
            .allowManager(p.getAllowManager())
            .individual(p.getIndividual())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .build();
    }

    private String toPublicMediaUrl(String maybeUrl) {
        if (maybeUrl == null) return null;
        String u = maybeUrl.trim();
        if (u.isEmpty()) return u;
        var m = PORTAL_FILE_URL.matcher(u);
        if (!m.matches()) return u;
        return "/v1/public/files/" + m.group(1) + "/download";
    }
}


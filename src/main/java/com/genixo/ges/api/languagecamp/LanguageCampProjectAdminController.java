package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectCreateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampProjectUpdateRequestDto;
import com.genixo.ges.company.repo.CompanyRepository;
import com.genixo.ges.languagecamp.model.LanguageCampProject;
import com.genixo.ges.languagecamp.repo.LanguageCampProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/language-camp-projects")
public class LanguageCampProjectAdminController {

    private final LanguageCampProjectRepository projects;
    private final CompanyRepository companies;

    public LanguageCampProjectAdminController(LanguageCampProjectRepository projects, CompanyRepository companies) {
        this.projects = projects;
        this.companies = companies;
    }

    @GetMapping
    @Operation(operationId = "adminLanguageCampProjectsList")
    public ResponseEntity<PageDto<LanguageCampProjectListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = projects.findAll(pageable);
        var items = p.getContent().stream().map(this::toListItemDto).toList();

        return ResponseEntity.ok(PageDto.<LanguageCampProjectListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Operation(operationId = "adminLanguageCampProjectsGet")
    public ResponseEntity<LanguageCampProjectDetailDto> get(@PathVariable UUID id) {
        LanguageCampProject p = projects.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found"));
        return ResponseEntity.ok(toDetailDto(p));
    }

    @PostMapping
    @Transactional
    @Operation(operationId = "adminLanguageCampProjectsCreate")
    public ResponseEntity<LanguageCampProjectDetailDto> create(@Valid @RequestBody LanguageCampProjectCreateRequestDto req) {
        var p = new LanguageCampProject();
        applyCreate(p, req);
        projects.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(p));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminLanguageCampProjectsUpdate")
    public ResponseEntity<LanguageCampProjectDetailDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody LanguageCampProjectUpdateRequestDto req
    ) {
        LanguageCampProject p = projects.findById(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found"));
        applyUpdate(p, req);
        projects.save(p);
        return ResponseEntity.ok(toDetailDto(p));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(operationId = "adminLanguageCampProjectsDelete")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!projects.existsById(id)) {
            throw new ApiProblemException(HttpStatus.NOT_FOUND, "Project not found");
        }
        projects.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyCreate(LanguageCampProject p, LanguageCampProjectCreateRequestDto req) {
        p.setTitle(req.getTitle());
        p.setCompany(req.getCompanyId() == null ? null : companies.findById(req.getCompanyId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Company not found")));
        p.setQuota(req.getQuota());
        p.setApplicationStartAt(req.getApplicationStartAt());
        p.setApplicationEndAt(req.getApplicationEndAt());
        p.setProjectStartAt(req.getProjectStartAt());
        p.setProjectEndAt(req.getProjectEndAt());
        p.setProjectStatus(req.getProjectStatus());
        p.setProjectType(req.getProjectType());
        p.setBanner(req.getBanner());
        p.setSmallBanner(req.getSmallBanner());
        p.setImages(req.getImages());
        p.setPresentationVideoUrl(req.getPresentationVideoUrl());
        p.setPresentationDocumentUrl(req.getPresentationDocumentUrl());
        p.setDescription(req.getDescription());
        p.setDuration(req.getDuration());
        p.setPrimaryLocations(req.getPrimaryLocations());
        p.setLocations(req.getLocations());
        p.setLocation(req.getLocation());
        p.setPrice(req.getPrice());
        p.setOriginalPrice(req.getOriginalPrice());
        p.setCurrency(req.getCurrency());
        p.setIncluded(req.getIncluded());
        p.setExcluded(req.getExcluded());
        p.setHighlights(req.getHighlights());
        p.setItinerary(req.getItinerary());
        p.setAllowParent(req.getAllowParent());
        p.setAllowTeacher(req.getAllowTeacher());
        p.setAllowManager(req.getAllowManager());
        p.setIndividual(req.getIndividual());
    }

    private void applyUpdate(LanguageCampProject p, LanguageCampProjectUpdateRequestDto req) {
        if (req.getTitle() != null) p.setTitle(req.getTitle());
        if (req.getCompanyId() != null) {
            p.setCompany(companies.findById(req.getCompanyId())
                .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Company not found")));
        }
        if (req.getQuota() != null) p.setQuota(req.getQuota());
        if (req.getApplicationStartAt() != null) p.setApplicationStartAt(req.getApplicationStartAt());
        if (req.getApplicationEndAt() != null) p.setApplicationEndAt(req.getApplicationEndAt());
        if (req.getProjectStartAt() != null) p.setProjectStartAt(req.getProjectStartAt());
        if (req.getProjectEndAt() != null) p.setProjectEndAt(req.getProjectEndAt());
        if (req.getProjectStatus() != null) p.setProjectStatus(req.getProjectStatus());
        if (req.getProjectType() != null) p.setProjectType(req.getProjectType());
        if (req.getBanner() != null) p.setBanner(req.getBanner());
        if (req.getSmallBanner() != null) p.setSmallBanner(req.getSmallBanner());
        if (req.getImages() != null) p.setImages(req.getImages());
        if (req.getPresentationVideoUrl() != null) p.setPresentationVideoUrl(req.getPresentationVideoUrl());
        if (req.getPresentationDocumentUrl() != null) p.setPresentationDocumentUrl(req.getPresentationDocumentUrl());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getDuration() != null) p.setDuration(req.getDuration());
        if (req.getPrimaryLocations() != null) p.setPrimaryLocations(req.getPrimaryLocations());
        if (req.getLocations() != null) p.setLocations(req.getLocations());
        if (req.getLocation() != null) p.setLocation(req.getLocation());
        if (req.getPrice() != null) p.setPrice(req.getPrice());
        if (req.getOriginalPrice() != null) p.setOriginalPrice(req.getOriginalPrice());
        if (req.getCurrency() != null) p.setCurrency(req.getCurrency());
        if (req.getIncluded() != null) p.setIncluded(req.getIncluded());
        if (req.getExcluded() != null) p.setExcluded(req.getExcluded());
        if (req.getHighlights() != null) p.setHighlights(req.getHighlights());
        if (req.getItinerary() != null) p.setItinerary(req.getItinerary());
        if (req.getAllowParent() != null) p.setAllowParent(req.getAllowParent());
        if (req.getAllowTeacher() != null) p.setAllowTeacher(req.getAllowTeacher());
        if (req.getAllowManager() != null) p.setAllowManager(req.getAllowManager());
        if (req.getIndividual() != null) p.setIndividual(req.getIndividual());
    }

    private LanguageCampProjectListItemDto toListItemDto(LanguageCampProject p) {
        return LanguageCampProjectListItemDto.builder()
            .id(p.getId())
            .title(p.getTitle())
            .companyId(p.getCompany() == null ? null : p.getCompany().getId())
            .individual(p.getIndividual())
            .projectStatus(p.getProjectStatus())
            .projectType(p.getProjectType())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
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
            .banner(p.getBanner())
            .smallBanner(p.getSmallBanner())
            .images(p.getImages())
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
}


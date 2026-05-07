package com.genixo.ges.api.catalog;

import com.genixo.ges.api.catalog.dto.CountryDto;
import com.genixo.ges.api.catalog.dto.DepartmentDto;
import com.genixo.ges.api.catalog.dto.UniversityDto;
import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.catalog.repo.CountryRepository;
import com.genixo.ges.catalog.repo.DepartmentRepository;
import com.genixo.ges.catalog.repo.UniversityRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/portal/catalog")
public class CatalogPortalController {

    private final CountryRepository countries;
    private final UniversityRepository universities;
    private final DepartmentRepository departments;

    public CatalogPortalController(CountryRepository countries, UniversityRepository universities, DepartmentRepository departments) {
        this.countries = countries;
        this.universities = universities;
        this.departments = departments;
    }

    @GetMapping("/countries")
    public ResponseEntity<PageDto<CountryDto>> countries(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q == null || q.isBlank()) ? countries.findAll(pageable) : countries.findByNameContainingIgnoreCase(q, pageable);
        return ResponseEntity.ok(PageDto.<CountryDto>builder()
            .items(p.getContent().stream().map(c -> CountryDto.builder().id(c.getId()).code(c.getCode()).name(c.getName()).build()).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/universities")
    public ResponseEntity<PageDto<UniversityDto>> universities(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) UUID countryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q != null && !q.isBlank())
            ? universities.findByNameContainingIgnoreCaseAndActiveTrue(q, pageable)
            : (countryId != null ? universities.findByCountry_IdAndActiveTrue(countryId, pageable) : universities.findByActiveTrue(pageable));

        return ResponseEntity.ok(PageDto.<UniversityDto>builder()
            .items(p.getContent().stream().map(u -> UniversityDto.builder()
                .id(u.getId())
                .name(u.getName())
                .active(u.isActive())
                .country(CountryDto.builder().id(u.getCountry().getId()).code(u.getCountry().getCode()).name(u.getCountry().getName()).build())
                .build()).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/departments")
    public ResponseEntity<PageDto<DepartmentDto>> departments(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q == null || q.isBlank()) ? departments.findByActiveTrue(pageable) : departments.findByNameContainingIgnoreCaseAndActiveTrue(q, pageable);
        return ResponseEntity.ok(PageDto.<DepartmentDto>builder()
            .items(p.getContent().stream().map(d -> DepartmentDto.builder().id(d.getId()).name(d.getName()).active(d.isActive()).build()).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }
}


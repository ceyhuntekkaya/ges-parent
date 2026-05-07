package com.genixo.ges.api.catalog;

import com.genixo.ges.api.catalog.dto.CountryDto;
import com.genixo.ges.api.catalog.dto.CountryUpsertRequestDto;
import com.genixo.ges.api.catalog.dto.DepartmentDto;
import com.genixo.ges.api.catalog.dto.DepartmentUpsertRequestDto;
import com.genixo.ges.api.catalog.dto.UniversityDto;
import com.genixo.ges.api.catalog.dto.UniversityUpsertRequestDto;
import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.catalog.model.Country;
import com.genixo.ges.catalog.model.Department;
import com.genixo.ges.catalog.model.University;
import com.genixo.ges.catalog.repo.CountryRepository;
import com.genixo.ges.catalog.repo.DepartmentRepository;
import com.genixo.ges.catalog.repo.UniversityRepository;
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
@RequestMapping("/v1/admin/catalog")
public class CatalogAdminController {

    private final CountryRepository countries;
    private final UniversityRepository universities;
    private final DepartmentRepository departments;

    public CatalogAdminController(CountryRepository countries, UniversityRepository universities, DepartmentRepository departments) {
        this.countries = countries;
        this.universities = universities;
        this.departments = departments;
    }

    // Countries
    @GetMapping("/countries")
    public ResponseEntity<PageDto<CountryDto>> listCountries(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q == null || q.isBlank()) ? countries.findAll(pageable) : countries.findByNameContainingIgnoreCase(q, pageable);
        return ResponseEntity.ok(PageDto.<CountryDto>builder()
            .items(p.getContent().stream().map(this::toCountryDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @PostMapping("/countries")
    @Transactional
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CountryUpsertRequestDto req) {
        countries.findByCodeIgnoreCase(req.getCode()).ifPresent(c -> {
            throw new ApiProblemException(HttpStatus.CONFLICT, "Country code already exists");
        });
        Country c = new Country();
        c.setCode(req.getCode().trim().toUpperCase());
        c.setName(req.getName().trim());
        countries.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCountryDto(c));
    }

    @PutMapping("/countries/{id}")
    @Transactional
    public ResponseEntity<CountryDto> updateCountry(@PathVariable UUID id, @Valid @RequestBody CountryUpsertRequestDto req) {
        Country c = countries.findById(id).orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Country not found"));
        c.setCode(req.getCode().trim().toUpperCase());
        c.setName(req.getName().trim());
        countries.save(c);
        return ResponseEntity.ok(toCountryDto(c));
    }

    @DeleteMapping("/countries/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCountry(@PathVariable UUID id) {
        if (!countries.existsById(id)) throw new ApiProblemException(HttpStatus.NOT_FOUND, "Country not found");
        countries.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Universities
    @GetMapping("/universities")
    public ResponseEntity<PageDto<UniversityDto>> listUniversities(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) UUID countryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q != null && !q.isBlank())
            ? universities.findByNameContainingIgnoreCaseAndActiveTrue(q, pageable) // admin sees only active via this query; keep simple for now
            : (countryId != null ? universities.findByCountry_IdAndActiveTrue(countryId, pageable) : universities.findByActiveTrue(pageable));

        return ResponseEntity.ok(PageDto.<UniversityDto>builder()
            .items(p.getContent().stream().map(this::toUniversityDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @PostMapping("/universities")
    @Transactional
    public ResponseEntity<UniversityDto> createUniversity(@Valid @RequestBody UniversityUpsertRequestDto req) {
        Country country = countries.findById(req.getCountryId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid countryId"));
        University u = new University();
        u.setCountry(country);
        u.setName(req.getName().trim());
        if (req.getActive() != null) u.setActive(req.getActive());
        universities.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUniversityDto(u));
    }

    @PutMapping("/universities/{id}")
    @Transactional
    public ResponseEntity<UniversityDto> updateUniversity(@PathVariable UUID id, @Valid @RequestBody UniversityUpsertRequestDto req) {
        University u = universities.findById(id).orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "University not found"));
        Country country = countries.findById(req.getCountryId())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid countryId"));
        u.setCountry(country);
        u.setName(req.getName().trim());
        if (req.getActive() != null) u.setActive(req.getActive());
        universities.save(u);
        return ResponseEntity.ok(toUniversityDto(u));
    }

    @DeleteMapping("/universities/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUniversity(@PathVariable UUID id) {
        if (!universities.existsById(id)) throw new ApiProblemException(HttpStatus.NOT_FOUND, "University not found");
        universities.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Departments
    @GetMapping("/departments")
    public ResponseEntity<PageDto<DepartmentDto>> listDepartments(
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        var p = (q == null || q.isBlank()) ? departments.findAll(pageable) : departments.findByNameContainingIgnoreCaseAndActiveTrue(q, pageable);
        return ResponseEntity.ok(PageDto.<DepartmentDto>builder()
            .items(p.getContent().stream().map(this::toDepartmentDto).toList())
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @PostMapping("/departments")
    @Transactional
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentUpsertRequestDto req) {
        Department d = new Department();
        d.setName(req.getName().trim());
        if (req.getActive() != null) d.setActive(req.getActive());
        departments.save(d);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDepartmentDto(d));
    }

    @PutMapping("/departments/{id}")
    @Transactional
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable UUID id, @Valid @RequestBody DepartmentUpsertRequestDto req) {
        Department d = departments.findById(id).orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Department not found"));
        d.setName(req.getName().trim());
        if (req.getActive() != null) d.setActive(req.getActive());
        departments.save(d);
        return ResponseEntity.ok(toDepartmentDto(d));
    }

    @DeleteMapping("/departments/{id}")
    @Transactional
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        if (!departments.existsById(id)) throw new ApiProblemException(HttpStatus.NOT_FOUND, "Department not found");
        departments.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CountryDto toCountryDto(Country c) {
        return CountryDto.builder().id(c.getId()).code(c.getCode()).name(c.getName()).build();
    }

    private UniversityDto toUniversityDto(University u) {
        return UniversityDto.builder()
            .id(u.getId())
            .name(u.getName())
            .active(u.isActive())
            .country(toCountryDto(u.getCountry()))
            .build();
    }

    private DepartmentDto toDepartmentDto(Department d) {
        return DepartmentDto.builder().id(d.getId()).name(d.getName()).active(d.isActive()).build();
    }
}


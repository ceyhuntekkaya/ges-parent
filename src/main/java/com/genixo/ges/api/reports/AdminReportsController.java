package com.genixo.ges.api.reports;

import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.api.reports.dto.ApplicationStatusCountDto;
import com.genixo.ges.api.reports.dto.ApplicationsSummaryReportDto;
import com.genixo.ges.api.reports.dto.ConsentStatsItemDto;
import com.genixo.ges.api.reports.dto.ConsentStatsReportDto;
import com.genixo.ges.api.reports.dto.MissingDocumentsReportDto;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.docreq.model.ApplicationDocument;
import com.genixo.ges.docreq.model.DocumentRequirement;
import com.genixo.ges.docreq.model.DocumentRequirementScope;
import com.genixo.ges.docreq.repo.ApplicationDocumentRepository;
import com.genixo.ges.docreq.repo.DocumentRequirementRepository;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import com.genixo.ges.university.repo.UniversityApplicationRepository;
import com.genixo.ges.legal.model.ConsentAcceptance;
import com.genixo.ges.legal.model.ConsentDocument;
import com.genixo.ges.legal.model.ConsentType;
import com.genixo.ges.legal.repo.ConsentAcceptanceRepository;
import com.genixo.ges.legal.repo.ConsentDocumentRepository;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/reports")
public class AdminReportsController {

    private final UniversityApplicationRepository universityApps;
    private final LanguageCampApplicationRepository languageCampApps;
    private final DocumentRequirementRepository requirements;
    private final ApplicationDocumentRepository documents;
    private final ConsentDocumentRepository consentDocs;
    private final ConsentAcceptanceRepository consentAcceptances;

    public AdminReportsController(
        UniversityApplicationRepository universityApps,
        LanguageCampApplicationRepository languageCampApps,
        DocumentRequirementRepository requirements,
        ApplicationDocumentRepository documents,
        ConsentDocumentRepository consentDocs,
        ConsentAcceptanceRepository consentAcceptances
    ) {
        this.universityApps = universityApps;
        this.languageCampApps = languageCampApps;
        this.requirements = requirements;
        this.documents = documents;
        this.consentDocs = consentDocs;
        this.consentAcceptances = consentAcceptances;
    }

    @GetMapping("/applications-summary")
    @Operation(operationId = "adminReportsApplicationsSummary")
    public ResponseEntity<?> applicationsSummary(@RequestParam(defaultValue = "json") String format) {
        ApplicationsSummaryReportDto dto = buildApplicationsSummary();

        if ("csv".equalsIgnoreCase(format)) {
            byte[] csv = toCsv(dto).getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"applications-summary.csv\"")
                .body(csv);
        }
        if (!"json".equalsIgnoreCase(format)) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid format (use json or csv)");
        }
        return ResponseEntity.ok(dto);
    }

    private ApplicationsSummaryReportDto buildApplicationsSummary() {
        Map<ApplicationStatus, Long> uniCounts = initZeroCounts();
        universityApps.findAll().forEach(a -> uniCounts.compute(a.getStatus(), (k, v) -> v + 1));

        Map<ApplicationStatus, Long> lcCounts = initZeroCounts();
        languageCampApps.findAll().forEach(a -> lcCounts.compute(a.getStatus(), (k, v) -> v + 1));

        return ApplicationsSummaryReportDto.builder()
            .universityApplications(toCountList(uniCounts))
            .languageCampApplications(toCountList(lcCounts))
            .build();
    }

    private static Map<ApplicationStatus, Long> initZeroCounts() {
        Map<ApplicationStatus, Long> m = new EnumMap<>(ApplicationStatus.class);
        Arrays.stream(ApplicationStatus.values()).forEach(s -> m.put(s, 0L));
        return m;
    }

    private static List<ApplicationStatusCountDto> toCountList(Map<ApplicationStatus, Long> m) {
        return Arrays.stream(ApplicationStatus.values())
            .map(s -> ApplicationStatusCountDto.builder().status(s).count(m.getOrDefault(s, 0L)).build())
            .toList();
    }

    private static String toCsv(ApplicationsSummaryReportDto dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("module,status,count\n");
        for (ApplicationStatusCountDto c : dto.getUniversityApplications()) {
            sb.append("UNIVERSITY,").append(c.getStatus()).append(",").append(c.getCount()).append("\n");
        }
        for (ApplicationStatusCountDto c : dto.getLanguageCampApplications()) {
            sb.append("LANGUAGE_CAMP,").append(c.getStatus()).append(",").append(c.getCount()).append("\n");
        }
        return sb.toString();
    }

    @GetMapping("/missing-documents")
    @Operation(operationId = "adminReportsMissingDocuments")
    public ResponseEntity<?> missingDocuments(
        @RequestParam DocumentRequirementScope scope,
        @RequestParam java.util.UUID applicationId,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "json") String format
    ) {
        MissingDocumentsReportDto dto = buildMissingDocuments(scope, applicationId, category);
        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("scope,applicationId,missingRequiredKey\n");
            for (String k : dto.getMissingRequiredKeys()) {
                sb.append(dto.getScope()).append(",").append(dto.getApplicationId()).append(",").append(k).append("\n");
            }
            byte[] csv = sb.toString().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"missing-documents.csv\"")
                .body(csv);
        }
        if (!"json".equalsIgnoreCase(format)) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid format (use json or csv)");
        }
        return ResponseEntity.ok(dto);
    }

    private MissingDocumentsReportDto buildMissingDocuments(DocumentRequirementScope scope, java.util.UUID applicationId, String category) {
        var reqPageable = org.springframework.data.domain.PageRequest.of(0, 1000, org.springframework.data.domain.Sort.by("key").ascending());
        List<DocumentRequirement> reqs = requirements.findByScopeAndActiveTrue(scope, reqPageable)
            .getContent()
            .stream()
            .filter(r -> category == null || category.isBlank()
                || (r.getCategory() != null && r.getCategory().equalsIgnoreCase(category)))
            .filter(DocumentRequirement::isRequired)
            .toList();

        var docPageable = org.springframework.data.domain.PageRequest.of(0, 2000, org.springframework.data.domain.Sort.by("uploadedAt").descending());
        List<ApplicationDocument> docs = documents.findByScopeAndApplicationId(scope, applicationId, docPageable).getContent();

        Set<String> uploadedKeys = new HashSet<>();
        for (ApplicationDocument d : docs) {
            if (d.getRequirementKey() != null && !d.getRequirementKey().isBlank()) {
                uploadedKeys.add(d.getRequirementKey());
            }
        }

        List<String> missing = reqs.stream()
            .map(DocumentRequirement::getKey)
            .filter(k -> !uploadedKeys.contains(k))
            .sorted()
            .toList();

        return MissingDocumentsReportDto.builder()
            .scope(scope)
            .applicationId(applicationId)
            .missingRequiredKeys(missing)
            .build();
    }

    @GetMapping("/consents")
    @Operation(operationId = "adminReportsConsents")
    public ResponseEntity<?> consentStats(@RequestParam(defaultValue = "json") String format) {
        ConsentStatsReportDto dto = buildConsentStats();

        if ("csv".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("type,activeDocuments,acceptances\n");
            for (ConsentStatsItemDto i : dto.getItems()) {
                sb.append(i.getType()).append(",").append(i.getActiveDocuments()).append(",").append(i.getAcceptances()).append("\n");
            }
            byte[] csv = sb.toString().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"consents.csv\"")
                .body(csv);
        }
        if (!"json".equalsIgnoreCase(format)) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Invalid format (use json or csv)");
        }
        return ResponseEntity.ok(dto);
    }

    private ConsentStatsReportDto buildConsentStats() {
        List<ConsentDocument> allDocs = consentDocs.findAll();
        List<ConsentAcceptance> allAcc = consentAcceptances.findAll();

        Map<ConsentType, Long> activeDocCounts = new EnumMap<>(ConsentType.class);
        Map<ConsentType, Long> accCounts = new EnumMap<>(ConsentType.class);
        Arrays.stream(ConsentType.values()).forEach(t -> {
            activeDocCounts.put(t, 0L);
            accCounts.put(t, 0L);
        });

        for (ConsentDocument d : allDocs) {
            if (d.isActive()) {
                activeDocCounts.compute(d.getType(), (k, v) -> v + 1);
            }
        }
        for (ConsentAcceptance a : allAcc) {
            if (a.getDocument() != null) {
                accCounts.compute(a.getDocument().getType(), (k, v) -> v + 1);
            }
        }

        List<ConsentStatsItemDto> items = Arrays.stream(ConsentType.values())
            .map(t -> ConsentStatsItemDto.builder()
                .type(t)
                .activeDocuments(activeDocCounts.getOrDefault(t, 0L))
                .acceptances(accCounts.getOrDefault(t, 0L))
                .build())
            .toList();

        return ConsentStatsReportDto.builder().items(items).build();
    }
}


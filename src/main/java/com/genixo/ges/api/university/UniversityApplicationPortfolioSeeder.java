package com.genixo.ges.api.university;

import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.PortfolioSection;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationPortfolioSection;
import com.genixo.ges.university.repo.PortfolioSectionRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UniversityApplicationPortfolioSeeder {

    private final PortfolioSectionRepository portfolioSections;

    public UniversityApplicationPortfolioSeeder(PortfolioSectionRepository portfolioSections) {
        this.portfolioSections = portfolioSections;
    }

    public void seedMatchingTemplates(UniversityApplication application) {
        List<PortfolioSection> templates = portfolioSections.findByActiveTrueOrderBySortOrderAscIdAsc();
        if (templates.isEmpty()) {
            return;
        }

        List<UniversityApplicationPortfolioSection> existing = application.getPortfolioSections();
        if (existing == null) {
            existing = new ArrayList<>();
        }

        Set<UUID> existingCatalogIds = new HashSet<>();
        for (UniversityApplicationPortfolioSection section : existing) {
            if (section.getPortfolioSection() != null && section.getPortfolioSection().getId() != null) {
                existingCatalogIds.add(section.getPortfolioSection().getId());
            }
        }

        EducationLevel level = application.getEducationLevel();
        List<String> departments = application.getDepartmentPreferences() == null
            ? List.of()
            : application.getDepartmentPreferences();

        for (PortfolioSection template : templates) {
            if (!matches(template, level, departments)) {
                continue;
            }
            UUID templateId = template.getId();
            if (templateId != null && existingCatalogIds.contains(templateId)) {
                continue;
            }

            UniversityApplicationPortfolioSection section = new UniversityApplicationPortfolioSection();
            section.setApplication(application);
            section.setPortfolioSection(template);
            section.setRequired(template.getDefaultRequired());
            section.setSortOrder(template.getSortOrder());
            existing.add(section);
            if (templateId != null) {
                existingCatalogIds.add(templateId);
            }
        }

        application.setPortfolioSections(existing);
    }

    private static boolean matches(PortfolioSection template, EducationLevel level, List<String> departments) {
        if (template.getEducationLevel() != null && template.getEducationLevel() != level) {
            return false;
        }

        String keyword = template.getDepartmentKeyword();
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String needle = keyword.trim().toLowerCase(Locale.ROOT);
        for (String dept : departments) {
            if (dept != null && dept.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }
}

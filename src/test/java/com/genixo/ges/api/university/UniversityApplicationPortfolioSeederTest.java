package com.genixo.ges.api.university;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.genixo.ges.university.model.EducationLevel;
import com.genixo.ges.university.model.PortfolioSection;
import com.genixo.ges.university.model.UniversityApplication;
import com.genixo.ges.university.model.UniversityApplicationPortfolioSection;
import com.genixo.ges.university.repo.PortfolioSectionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UniversityApplicationPortfolioSeederTest {

    @Mock
    PortfolioSectionRepository portfolioSections;

    @InjectMocks
    UniversityApplicationPortfolioSeeder seeder;

    @Test
    void seedMatchingTemplates_addsSectionsForEducationLevelAndDepartment() {
        PortfolioSection general = template("Proje", null, null, 10);
        PortfolioSection masterOnly = template("Araştırma", EducationLevel.MASTER, null, 20);
        PortfolioSection archOnly = template("Sanat", null, "mimar", 30);

        when(portfolioSections.findByActiveTrueOrderBySortOrderAscIdAsc())
            .thenReturn(List.of(general, masterOnly, archOnly));

        UniversityApplication application = new UniversityApplication();
        application.setEducationLevel(EducationLevel.MASTER);
        application.setDepartmentPreferences(List.of("Mimarlık"));

        seeder.seedMatchingTemplates(application);

        List<UniversityApplicationPortfolioSection> sections = application.getPortfolioSections();
        assertThat(sections).hasSize(3);
        assertThat(sections)
            .extracting(s -> s.getPortfolioSection().getName())
            .containsExactly("Proje", "Araştırma", "Sanat");
    }

    private static PortfolioSection template(String name, EducationLevel level, String keyword, int sort) {
        PortfolioSection s = new PortfolioSection();
        s.setName(name);
        s.setEducationLevel(level);
        s.setDepartmentKeyword(keyword);
        s.setSortOrder(sort);
        s.setDefaultRequired(false);
        s.setActive(true);
        return s;
    }
}

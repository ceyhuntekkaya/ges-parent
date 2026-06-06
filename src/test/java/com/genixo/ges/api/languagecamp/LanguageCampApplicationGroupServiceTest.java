package com.genixo.ges.api.languagecamp;

import static org.assertj.core.api.Assertions.assertThat;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationGroupListItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LanguageCampApplicationGroupServiceTest {

    @Autowired
    private LanguageCampApplicationGroupService groups;

    @Test
    void listAdminGroups_returnsPagedGroups() {
        PageDto<LanguageCampApplicationGroupListItemDto> page = groups.listAdminGroups(0, 5, null, null, null);

        assertThat(page).isNotNull();
        assertThat(page.getItems()).isNotNull();
    }
}

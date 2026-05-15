package com.genixo.ges.api.languagecamp;

import static org.assertj.core.api.Assertions.assertThat;

import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDocumentDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampVisaFormUpsertRequestDto;
import com.genixo.ges.api.storage.dto.StoredFileDto;
import com.genixo.ges.languagecamp.model.PassportType;
import com.genixo.ges.storage.model.StoredFilePurpose;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
class LanguageCampVisaFormDtoSerializationTest {

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void serializesVisaFormWithNestedDocumentFile() throws Exception {
        StoredFileDto file = StoredFileDto.builder()
            .id(UUID.randomUUID())
            .purpose(StoredFilePurpose.LANGUAGE_CAMP_VISA_DOCUMENT)
            .originalFilename("passport.pdf")
            .contentType("application/pdf")
            .sizeBytes(42)
            .build();

        LanguageCampVisaFormDocumentDto document = LanguageCampVisaFormDocumentDto.builder()
            .id(UUID.randomUUID())
            .file(file)
            .build();

        LanguageCampVisaFormDto dto = LanguageCampVisaFormDto.builder()
            .id(UUID.randomUUID())
            .applicationId(UUID.randomUUID())
            .documents(List.of(document))
            .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"documents\"");
        assertThat(json).contains("\"file\"");
        assertThat(json).contains("\"contentType\":\"application/pdf\"");
    }

    @Test
    void deserializesVisaFormUpsertRequest() throws Exception {
        String json = """
            {
              "passportNumber": "U12345678",
              "passportValidUntil": "2030-12-31",
              "passportType": "ORDINARY",
              "visaValidFrom": "2026-06-01",
              "visaValidUntil": "2026-09-01",
              "visaIssuingCountry": "Germany",
              "visaType": "Schengen C"
            }
            """;

        LanguageCampVisaFormUpsertRequestDto dto =
            objectMapper.readValue(json, LanguageCampVisaFormUpsertRequestDto.class);

        assertThat(dto.getPassportNumber()).isEqualTo("U12345678");
        assertThat(dto.getPassportValidUntil()).isEqualTo(LocalDate.of(2030, 12, 31));
        assertThat(dto.getPassportType()).isEqualTo(PassportType.ORDINARY);
        assertThat(dto.getVisaIssuingCountry()).isEqualTo("Germany");
        assertThat(dto.getVisaType()).isEqualTo("Schengen C");
    }
}

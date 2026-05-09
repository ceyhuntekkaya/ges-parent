package com.genixo.ges.api.languagecamp.dto;

import com.genixo.ges.languagecamp.model.LanguageCampCategory;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Value;

@Value
public class LanguageCampApplicationCreateRequestDto {
    @NotNull
    LanguageCampCategory category;

    String firstName;
    String lastName;
    LocalDate birthDate;
    String phone;
    Boolean isItSelf;
    Integer numberOfApplicant;

    Boolean under18;
    String parentFullName;
    String parentPhoneNumber;
    String parentEmailAddress;
    String parentRelationship;
    String userNotes;
}


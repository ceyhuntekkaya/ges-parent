package com.genixo.ges.api.university.dto;

import com.genixo.ges.application.model.ApplicationStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UniversityApplicationByStatusListItemDto {

    UUID id;
    String applicantFirstName;
    String applicantLastName;
    String followerPerson;
    ApplicationStatus status;
}

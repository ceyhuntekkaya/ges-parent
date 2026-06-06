package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationAdminUpdateRequestDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDocumentDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationMeetingDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationNoteDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationPaymentDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationTaskDto;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationDocument;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationMeeting;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationNote;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationPayment;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationTask;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

final class LanguageCampApplicationDtoMapper {

    private LanguageCampApplicationDtoMapper() {}

    static LanguageCampApplicationListItemDto toListItemDto(LanguageCampApplication a) {
        List<LanguageCampApplicationTask> tasks = a.getTasks() == null ? List.of() : a.getTasks();
        List<LanguageCampApplicationMeeting> meetings = a.getMeetings() == null ? List.of() : a.getMeetings();
        List<LanguageCampApplicationDocument> documents = a.getDocuments() == null ? List.of() : a.getDocuments();
        List<LanguageCampApplicationPayment> payments = a.getPayments() == null ? List.of() : a.getPayments();

        List<java.time.Instant> pendingTaskScheduledAts = tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
            .map(LanguageCampApplicationTask::getScheduledAt)
            .filter(d -> d != null)
            .sorted(Comparator.naturalOrder())
            .toList();
        int pendingTaskCount = (int) tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.PENDING)
            .count();
        int completedTaskCount = (int) tasks.stream()
            .filter(t -> t.getStatus() == UniversityApplicationTaskStatus.DONE)
            .count();
        int documentsWithFileCount = (int) documents.stream()
            .filter(d -> d.getDocumentUrl() != null && !d.getDocumentUrl().isBlank())
            .count();
        BigDecimal totalPaidAmount = payments.stream()
            .map(LanguageCampApplicationPayment::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return LanguageCampApplicationListItemDto.builder()
            .id(a.getId())
            .firstName(a.getFirstName())
            .lastName(a.getLastName())
            .isItSelf(a.getIsItSelf())
            .status(a.getStatus())
            .category(a.getCategory())
            .languageCampProjectId(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getId())
            .languageCampProjectTitle(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getTitle())
            .paymentCompleted(a.isPaymentCompleted())
            .followerPerson(a.getFollowerPerson())
            .priceAmount(a.getPriceAmount())
            .priceCurrency(a.getPriceCurrency())
            .totalPaidAmount(totalPaidAmount)
            .pendingTaskCount(pendingTaskCount)
            .completedTaskCount(completedTaskCount)
            .pendingTaskScheduledAts(pendingTaskScheduledAts)
            .meetingCount(meetings.size())
            .documentCount(documents.size())
            .documentsWithFileCount(documentsWithFileCount)
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }

    static LanguageCampApplicationDetailDto toDetailDto(LanguageCampApplication a) {
        return LanguageCampApplicationDetailDto.builder()
            .id(a.getId())
            .status(a.getStatus())
            .category(a.getCategory())
            .languageCampProjectId(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getId())
            .languageCampProjectTitle(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getTitle())
            .accommodationType(a.getAccommodationType())
            .visaNeeded(a.getVisaNeeded())
            .visaFollowByGes(a.getVisaFollowByGes())
            .emergencyContact(a.getEmergencyContact())
            .paymentPreference(a.getPaymentPreference())
            .paymentCompleted(a.isPaymentCompleted())
            .payments(toPaymentDtos(a.getPayments()))
            .priceAmount(a.getPriceAmount())
            .priceCurrency(a.getPriceCurrency())
            .kvkkAcceptedAt(a.getKvkkAcceptedAt())
            .companyId(a.getCompany() == null ? null : a.getCompany().getId())
            .company(a.getCompany() == null ? null : com.genixo.ges.api.languagecamp.dto.CompanyDto.builder()
                .id(a.getCompany().getId())
                .code(a.getCompany().getCode())
                .name(a.getCompany().getName())
                .taxNumber(a.getCompany().getTaxNumber())
                .contactFullName(a.getCompany().getContactFullName())
                .contactPhone(a.getCompany().getContactPhone())
                .contactEmail(a.getCompany().getContactEmail())
                .createdAt(a.getCompany().getCreatedAt())
                .updatedAt(a.getCompany().getUpdatedAt())
                .build())
            .firstName(a.getFirstName())
            .lastName(a.getLastName())
            .birthDate(a.getBirthDate())
            .phone(a.getPhone())
            .isItSelf(a.getIsItSelf())
            .numberOfApplicant(a.getNumberOfApplicant())
            .under18(a.getUnder18())
            .parentFullName(a.getParentFullName())
            .parentPhoneNumber(a.getParentPhoneNumber())
            .parentEmailAddress(a.getParentEmailAddress())
            .parentRelationship(a.getParentRelationship())
            .userNotes(a.getUserNotes())
            .followerPerson(a.getFollowerPerson())
            .notes(a.getNotes())
            .applicationNotes(toNoteDtos(a.getApplicationNotes()))
            .meetings(toMeetingDtos(a.getMeetings()))
            .tasks(toTaskDtos(a.getTasks()))
            .documents(toDocumentDtos(a.getDocuments()))
            .visaForm(LanguageCampVisaFormDtoMapper.toDto(a.getVisaForm()))
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
    }

    static void applyAdminPatch(LanguageCampApplication a, LanguageCampApplicationAdminUpdateRequestDto req) {
        if (req.getFirstName() != null) a.setFirstName(req.getFirstName());
        if (req.getLastName() != null) a.setLastName(req.getLastName());
        if (req.getBirthDate() != null) a.setBirthDate(req.getBirthDate());
        if (req.getPhone() != null) a.setPhone(req.getPhone());
        if (req.getIsItSelf() != null) a.setIsItSelf(req.getIsItSelf());
        if (req.getNumberOfApplicant() != null) a.setNumberOfApplicant(req.getNumberOfApplicant());
        if (req.getAccommodationType() != null) a.setAccommodationType(req.getAccommodationType());
        if (req.getVisaNeeded() != null) a.setVisaNeeded(req.getVisaNeeded());
        if (req.getVisaFollowByGes() != null) a.setVisaFollowByGes(req.getVisaFollowByGes());
        if (req.getEmergencyContact() != null) a.setEmergencyContact(req.getEmergencyContact());
        if (req.getPaymentPreference() != null) a.setPaymentPreference(req.getPaymentPreference());
        if (req.getUnder18() != null) a.setUnder18(req.getUnder18());
        if (req.getParentFullName() != null) a.setParentFullName(req.getParentFullName());
        if (req.getParentPhoneNumber() != null) a.setParentPhoneNumber(req.getParentPhoneNumber());
        if (req.getParentEmailAddress() != null) a.setParentEmailAddress(req.getParentEmailAddress());
        if (req.getParentRelationship() != null) a.setParentRelationship(req.getParentRelationship());
        if (req.getUserNotes() != null) a.setUserNotes(req.getUserNotes());
        if (req.getFollowerPerson() != null) a.setFollowerPerson(req.getFollowerPerson());
        if (req.getNotes() != null) a.setNotes(req.getNotes());
        if (req.getPriceAmount() != null) a.setPriceAmount(req.getPriceAmount());
        if (req.getPriceCurrency() != null) a.setPriceCurrency(req.getPriceCurrency());
    }

    private static List<LanguageCampApplicationPaymentDto> toPaymentDtos(List<LanguageCampApplicationPayment> payments) {
        if (payments == null) {
            return List.of();
        }
        return payments.stream()
            .map(p -> LanguageCampApplicationPaymentDto.builder()
                .id(p.getId())
                .paymentAt(p.getPaymentAt())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .receivedBy(p.getReceivedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build())
            .toList();
    }

    private static List<LanguageCampApplicationNoteDto> toNoteDtos(List<LanguageCampApplicationNote> notes) {
        if (notes == null) {
            return List.of();
        }
        return notes.stream()
            .map(n -> LanguageCampApplicationNoteDto.builder()
                .id(n.getId())
                .writtenBy(n.getWrittenBy())
                .writtenAt(n.getWrittenAt())
                .todoText(n.getTodoText())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build())
            .toList();
    }

    private static List<LanguageCampApplicationMeetingDto> toMeetingDtos(List<LanguageCampApplicationMeeting> meetings) {
        if (meetings == null) {
            return List.of();
        }
        return meetings.stream()
            .map(m -> LanguageCampApplicationMeetingDto.builder()
                .id(m.getId())
                .person(m.getPerson())
                .meetingAt(m.getMeetingAt())
                .meetingNote(m.getMeetingNote())
                .meetingResult(m.getMeetingResult())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build())
            .toList();
    }

    private static List<LanguageCampApplicationTaskDto> toTaskDtos(List<LanguageCampApplicationTask> tasks) {
        if (tasks == null) {
            return List.of();
        }
        return tasks.stream()
            .map(t -> LanguageCampApplicationTaskDto.builder()
                .id(t.getId())
                .scheduledAt(t.getScheduledAt())
                .withWhom(t.getWithWhom())
                .whatToDo(t.getWhatToDo())
                .status(t.getStatus())
                .performedByUser(t.getPerformedByUser())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build())
            .toList();
    }

    private static List<LanguageCampApplicationDocumentDto> toDocumentDtos(List<LanguageCampApplicationDocument> documents) {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
            .map(d -> LanguageCampApplicationDocumentDto.builder()
                .id(d.getId())
                .required(d.getRequired())
                .documentName(d.getDocumentName())
                .documentDescription(d.getDocumentDescription())
                .documentUrl(d.getDocumentUrl())
                .uploadedAt(d.getUploadedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build())
            .toList();
    }
}

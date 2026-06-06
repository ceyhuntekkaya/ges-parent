package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationGroupListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationGroupParticipantSummaryDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.application.model.ApplicationStatus;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationPayment;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationGroupKeyRow;
import com.genixo.ges.languagecamp.repo.LanguageCampApplicationRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LanguageCampApplicationGroupService {

    private final LanguageCampApplicationRepository apps;

    public LanguageCampApplicationGroupService(LanguageCampApplicationRepository apps) {
        this.apps = apps;
    }

    @Transactional(readOnly = true)
    public PageDto<LanguageCampApplicationGroupListItemDto> listAdminGroups(
        int page,
        int size,
        UUID projectId,
        ApplicationStatus status,
        Boolean paymentCompleted
    ) {
        var pageable = PageRequest.of(page, size, Sort.unsorted());
        Page<LanguageCampApplicationGroupKeyRow> keys = apps.findAdminGroupKeys(
            projectId,
            status == null ? null : status.name(),
            paymentCompleted,
            pageable
        );

        List<LanguageCampApplicationGroupListItemDto> items = new ArrayList<>();
        for (LanguageCampApplicationGroupKeyRow key : keys.getContent()) {
            List<LanguageCampApplication> participants = apps.findAdminGroupApplications(
                key.getApplicantUserId(),
                key.getLanguageCampProjectId()
            );
            items.add(toGroupListItem(participants, key));
        }

        return PageDto.<LanguageCampApplicationGroupListItemDto>builder()
            .items(items)
            .page(keys.getNumber())
            .size(keys.getSize())
            .totalItems(keys.getTotalElements())
            .totalPages(keys.getTotalPages())
            .build();
    }

    @Transactional(readOnly = true)
    public LanguageCampApplicationDetailDto enrichDetail(LanguageCampApplication current) {
        LanguageCampApplicationDetailDto base = LanguageCampApplicationDtoMapper.toDetailDto(current);
        if (current.getApplicant() == null || current.getLanguageCampProject() == null) {
            return base;
        }

        List<LanguageCampApplication> siblings = apps.findAdminGroupApplications(
            current.getApplicant().getId(),
            current.getLanguageCampProject().getId()
        );
        return applyGroupContext(base, current, siblings);
    }

    static LanguageCampApplicationGroupListItemDto toGroupListItem(
        List<LanguageCampApplication> participants,
        LanguageCampApplicationGroupKeyRow key
    ) {
        List<LanguageCampApplication> sorted = sortedParticipants(participants);
        LanguageCampApplication primary = sorted.isEmpty() ? null : sorted.get(0);
        UserAccount applicant = sorted.stream()
            .map(LanguageCampApplication::getApplicant)
            .filter(a -> a != null)
            .findFirst()
            .orElse(null);

        List<LanguageCampApplicationListItemDto> participantDtos = sorted.stream()
            .map(LanguageCampApplicationDtoMapper::toListItemDto)
            .toList();

        return LanguageCampApplicationGroupListItemDto.builder()
            .applicantUserId(key.getApplicantUserId())
            .applicantEmail(applicant == null ? null : applicant.getEmail())
            .applicantDisplayName(resolveApplicantDisplayName(applicant, sorted))
            .languageCampProjectId(key.getLanguageCampProjectId())
            .languageCampProjectTitle(primary == null || primary.getLanguageCampProject() == null
                ? null
                : primary.getLanguageCampProject().getTitle())
            .category(primary == null ? null : primary.getCategory())
            .participantCount(key.getParticipantCount() == null ? sorted.size() : key.getParticipantCount().intValue())
            .primaryApplicationId(primary == null ? null : primary.getId())
            .participants(participantDtos)
            .createdAt(key.getCreatedAt())
            .updatedAt(key.getUpdatedAt())
            .build();
    }

    static LanguageCampApplicationDetailDto applyGroupContext(
        LanguageCampApplicationDetailDto base,
        LanguageCampApplication current,
        List<LanguageCampApplication> siblings
    ) {
        List<LanguageCampApplication> sorted = sortedParticipants(siblings);
        int count = sorted.size();
        int index = 0;
        List<LanguageCampApplicationGroupParticipantSummaryDto> summaries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            LanguageCampApplication p = sorted.get(i);
            if (p.getId() != null && p.getId().equals(current.getId())) {
                index = i + 1;
            }
            summaries.add(LanguageCampApplicationGroupParticipantSummaryDto.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .status(p.getStatus())
                .isItSelf(p.getIsItSelf())
                .participantIndex(i + 1)
                .paymentCompleted(p.isPaymentCompleted())
                .priceAmount(p.getPriceAmount())
                .priceCurrency(p.getPriceCurrency())
                .totalPaidAmount(sumPayments(p.getPayments()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build());
        }

        UserAccount applicant = current.getApplicant();
        return LanguageCampApplicationDetailDto.builder()
            .id(base.getId())
            .status(base.getStatus())
            .category(base.getCategory())
            .languageCampProjectId(base.getLanguageCampProjectId())
            .languageCampProjectTitle(base.getLanguageCampProjectTitle())
            .accommodationType(base.getAccommodationType())
            .visaNeeded(base.getVisaNeeded())
            .visaFollowByGes(base.getVisaFollowByGes())
            .emergencyContact(base.getEmergencyContact())
            .paymentPreference(base.getPaymentPreference())
            .paymentCompleted(base.isPaymentCompleted())
            .payments(base.getPayments())
            .priceAmount(base.getPriceAmount())
            .priceCurrency(base.getPriceCurrency())
            .kvkkAcceptedAt(base.getKvkkAcceptedAt())
            .companyId(base.getCompanyId())
            .company(base.getCompany())
            .firstName(base.getFirstName())
            .lastName(base.getLastName())
            .birthDate(base.getBirthDate())
            .phone(base.getPhone())
            .isItSelf(base.getIsItSelf())
            .numberOfApplicant(count)
            .under18(base.getUnder18())
            .parentFullName(base.getParentFullName())
            .parentPhoneNumber(base.getParentPhoneNumber())
            .parentEmailAddress(base.getParentEmailAddress())
            .parentRelationship(base.getParentRelationship())
            .userNotes(base.getUserNotes())
            .followerPerson(base.getFollowerPerson())
            .notes(base.getNotes())
            .applicationNotes(base.getApplicationNotes())
            .meetings(base.getMeetings())
            .tasks(base.getTasks())
            .documents(base.getDocuments())
            .visaForm(base.getVisaForm())
            .applicantUserId(applicant == null ? null : applicant.getId())
            .applicantEmail(applicant == null ? null : applicant.getEmail())
            .applicantDisplayName(resolveApplicantDisplayName(applicant, sorted))
            .participantIndex(index == 0 ? null : index)
            .participantCount(count)
            .groupParticipants(summaries)
            .createdAt(base.getCreatedAt())
            .updatedAt(base.getUpdatedAt())
            .build();
    }

    static List<LanguageCampApplication> sortedParticipants(List<LanguageCampApplication> participants) {
        return participants.stream()
            .sorted(Comparator.comparing(LanguageCampApplication::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    static String resolveApplicantDisplayName(UserAccount applicant, List<LanguageCampApplication> participants) {
        if (applicant != null) {
            ApplicantProfile profile = applicant.getApplicantProfile();
            if (profile != null) {
                String name = ((profile.getFirstName() == null ? "" : profile.getFirstName())
                    + " "
                    + (profile.getLastName() == null ? "" : profile.getLastName())).trim();
                if (!name.isEmpty()) {
                    return name;
                }
            }
            if (applicant.getEmail() != null && !applicant.getEmail().isBlank()) {
                return applicant.getEmail();
            }
        }

        return participants.stream()
            .filter(p -> Boolean.TRUE.equals(p.getIsItSelf()))
            .map(LanguageCampApplicationGroupService::participantFullName)
            .filter(name -> !name.isBlank())
            .findFirst()
            .orElseGet(() -> participants.stream()
                .map(LanguageCampApplicationGroupService::participantFullName)
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElse("-"));
    }

    private static String participantFullName(LanguageCampApplication p) {
        return ((p.getFirstName() == null ? "" : p.getFirstName())
            + " "
            + (p.getLastName() == null ? "" : p.getLastName())).trim();
    }

    private static BigDecimal sumPayments(List<LanguageCampApplicationPayment> payments) {
        if (payments == null || payments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return payments.stream()
            .map(LanguageCampApplicationPayment::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

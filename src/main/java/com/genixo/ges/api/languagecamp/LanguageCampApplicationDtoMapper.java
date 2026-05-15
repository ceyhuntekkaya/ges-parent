package com.genixo.ges.api.languagecamp;

import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationDetailDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationListItemDto;
import com.genixo.ges.api.languagecamp.dto.LanguageCampApplicationPaymentDto;
import com.genixo.ges.languagecamp.model.LanguageCampApplication;
import com.genixo.ges.languagecamp.model.LanguageCampApplicationPayment;
import java.util.List;

final class LanguageCampApplicationDtoMapper {

    private LanguageCampApplicationDtoMapper() {}

    static LanguageCampApplicationListItemDto toListItemDto(LanguageCampApplication a) {
        return LanguageCampApplicationListItemDto.builder()
            .id(a.getId())
            .firstName(a.getFirstName())
            .lastName(a.getLastName())
            .status(a.getStatus())
            .category(a.getCategory())
            .languageCampProjectId(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getId())
            .languageCampProjectTitle(a.getLanguageCampProject() == null ? null : a.getLanguageCampProject().getTitle())
            .paymentCompleted(a.isPaymentCompleted())
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
            .visaForm(LanguageCampVisaFormDtoMapper.toDto(a.getVisaForm()))
            .createdAt(a.getCreatedAt())
            .updatedAt(a.getUpdatedAt())
            .build();
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
}

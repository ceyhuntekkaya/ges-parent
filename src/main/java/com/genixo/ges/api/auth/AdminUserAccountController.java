package com.genixo.ges.api.auth;

import com.genixo.ges.api.auth.dto.UserAccountAdminDetailDto;
import com.genixo.ges.api.auth.dto.UserAccountAdminListItemDto;
import com.genixo.ges.api.common.dto.PageDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.applicant.model.ApplicantProfile;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.model.UserRole;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.common.jpa.Address;
import io.swagger.v3.oas.annotations.Operation;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/users")
public class AdminUserAccountController {

    private final UserAccountRepository users;

    public AdminUserAccountController(UserAccountRepository users) {
        this.users = users;
    }

    @GetMapping
    @Operation(operationId = "adminUsersList")
    public ResponseEntity<PageDto<UserAccountAdminListItemDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) UserRole role,
        @RequestParam(required = false) String q
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String qq = q == null ? null : q.trim();
        if (qq != null && qq.isEmpty()) {
            qq = null;
        }
        String textPattern = qq == null ? null : "%" + qq.toLowerCase() + "%";
        var p = users.searchForAdmin(role, textPattern, pageable);
        var items = p.getContent().stream().map(this::toListItem).toList();
        return ResponseEntity.ok(PageDto.<UserAccountAdminListItemDto>builder()
            .items(items)
            .page(p.getNumber())
            .size(p.getSize())
            .totalItems(p.getTotalElements())
            .totalPages(p.getTotalPages())
            .build());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(operationId = "adminUsersGet")
    public ResponseEntity<UserAccountAdminDetailDto> get(@PathVariable UUID id) {
        UserAccount u = users.findByIdWithApplicantProfile(id)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı."));
        if (u.getRole() != UserRole.USER) {
            throw new ApiProblemException(HttpStatus.BAD_REQUEST, "Yalnızca USER rolündeki hesaplar bu ekranda kullanılabilir.");
        }
        return ResponseEntity.ok(toDetailDto(u));
    }

    private static UserAccountAdminDetailDto toDetailDto(UserAccount u) {
        ApplicantProfile ap = u.getApplicantProfile();
        return UserAccountAdminDetailDto.builder()
            .id(u.getId())
            .email(u.getEmail())
            .role(u.getRole())
            .status(u.getStatus())
            .createdAt(u.getCreatedAt())
            .applicantFirstName(ap == null ? null : ap.getFirstName())
            .applicantLastName(ap == null ? null : ap.getLastName())
            .birthDate(ap == null ? null : ap.getBirthDate())
            .phone(ap == null ? null : ap.getPhone())
            .nationality(ap == null ? null : ap.getNationality())
            .address(ap == null ? null : flattenAddress(ap))
            .build();
    }

    private static String flattenAddress(ApplicantProfile ap) {
        Address a = ap.getAddress();
        if (a == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        appendPart(sb, a.getLine1());
        appendPart(sb, a.getLine2());
        appendPart(sb, a.getDistrict());
        appendPart(sb, a.getCity());
        appendPart(sb, a.getCountry());
        appendPart(sb, a.getPostalCode());
        String s = sb.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isBlank()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(", ");
        }
        sb.append(part.trim());
    }

    private UserAccountAdminListItemDto toListItem(UserAccount u) {
        ApplicantProfile ap = u.getApplicantProfile();
        return UserAccountAdminListItemDto.builder()
            .id(u.getId())
            .email(u.getEmail())
            .applicantFirstName(ap == null ? null : ap.getFirstName())
            .applicantLastName(ap == null ? null : ap.getLastName())
            .role(u.getRole())
            .status(u.getStatus())
            .createdAt(u.getCreatedAt())
            .build();
    }
}

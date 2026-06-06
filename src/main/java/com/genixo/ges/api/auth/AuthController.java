package com.genixo.ges.api.auth;

import com.genixo.ges.api.auth.dto.AuthTokensDto;
import com.genixo.ges.api.auth.dto.LoginRequestDto;
import com.genixo.ges.api.auth.dto.MeDto;
import com.genixo.ges.api.auth.dto.RefreshRequestDto;
import com.genixo.ges.api.auth.dto.RegisterRequestDto;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService auth;
    private final UserAccountRepository users;

    public AuthController(AuthService auth, UserAccountRepository users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/login")
    @Operation(operationId = "authLogin")
    public ResponseEntity<AuthTokensDto> login(@Valid @RequestBody LoginRequestDto req, HttpServletRequest http) {
        String ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown");
        String ip = Optional.ofNullable(http.getRemoteAddr()).orElse("unknown");
        return ResponseEntity.ok(auth.login(req.getEmail(), req.getPassword(), ua, ip));
    }

    @PostMapping("/register")
    @Operation(operationId = "authRegister")
    public ResponseEntity<AuthTokensDto> register(@Valid @RequestBody RegisterRequestDto req, HttpServletRequest http) {
        String ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown");
        String ip = Optional.ofNullable(http.getRemoteAddr()).orElse("unknown");
        return ResponseEntity.ok(auth.register(req.getEmail(), req.getPassword(), req.getRole(), ua, ip));
    }

    @PostMapping("/refresh")
    @Operation(operationId = "authRefresh")
    public ResponseEntity<AuthTokensDto> refresh(@Valid @RequestBody RefreshRequestDto req, HttpServletRequest http) {
        String ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown");
        String ip = Optional.ofNullable(http.getRemoteAddr()).orElse("unknown");
        return ResponseEntity.ok(auth.refresh(req.getRefreshToken(), ua, ip));
    }

    @PostMapping("/logout")
    @Operation(operationId = "authLogout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDto req) {
        auth.logout(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(operationId = "authMe")
    public ResponseEntity<MeDto> me(@AuthenticationPrincipal AuthUserPrincipal principal) {
        // principal has minimal fields; return canonical values from DB
        var ua = users.findByIdWithApplicantProfile(principal.getId()).orElseThrow();
        return ResponseEntity.ok(MeDto.builder()
            .id(ua.getId())
            .email(ua.getEmail())
            .displayName(displayNameFor(ua))
            .role(ua.getRole())
            .status(ua.getStatus())
            .build());
    }

    private static String displayNameFor(UserAccount ua) {
        var profile = ua.getApplicantProfile();
        if (profile != null) {
            String joined = Stream.of(profile.getFirstName(), profile.getLastName())
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
            if (!joined.isBlank()) {
                return joined;
            }
        }
        return ua.getEmail();
    }
}


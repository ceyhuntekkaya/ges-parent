package com.genixo.ges.api.auth;

import com.genixo.ges.api.auth.dto.AuthTokensDto;
import com.genixo.ges.api.auth.dto.LoginRequestDto;
import com.genixo.ges.api.auth.dto.MeDto;
import com.genixo.ges.api.auth.dto.RefreshRequestDto;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.AuthUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
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
    public ResponseEntity<AuthTokensDto> login(@Valid @RequestBody LoginRequestDto req, HttpServletRequest http) {
        String ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown");
        String ip = Optional.ofNullable(http.getRemoteAddr()).orElse("unknown");
        return ResponseEntity.ok(auth.login(req.getEmail(), req.getPassword(), ua, ip));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokensDto> refresh(@Valid @RequestBody RefreshRequestDto req, HttpServletRequest http) {
        String ua = Optional.ofNullable(http.getHeader("User-Agent")).orElse("unknown");
        String ip = Optional.ofNullable(http.getRemoteAddr()).orElse("unknown");
        return ResponseEntity.ok(auth.refresh(req.getRefreshToken(), ua, ip));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequestDto req) {
        auth.logout(req.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeDto> me(@AuthenticationPrincipal AuthUserPrincipal principal) {
        // principal has minimal fields; return canonical values from DB
        var ua = users.findById(principal.getId()).orElseThrow();
        return ResponseEntity.ok(MeDto.builder()
            .id(ua.getId())
            .email(ua.getEmail())
            .role(ua.getRole())
            .status(ua.getStatus())
            .build());
    }
}


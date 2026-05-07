package com.genixo.ges.api.auth;

import com.genixo.ges.api.auth.dto.AuthTokensDto;
import com.genixo.ges.api.common.exception.ApiProblemException;
import com.genixo.ges.auth.model.RefreshToken;
import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.model.UserStatus;
import com.genixo.ges.auth.repo.RefreshTokenRepository;
import com.genixo.ges.auth.repo.UserAccountRepository;
import com.genixo.ges.security.JwtProperties;
import com.genixo.ges.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final JwtService jwt;
    private final JwtProperties props;

    public AuthService(
        AuthenticationManager authManager,
        UserAccountRepository users,
        RefreshTokenRepository refreshTokens,
        JwtService jwt,
        JwtProperties props
    ) {
        this.authManager = authManager;
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.jwt = jwt;
        this.props = props;
    }

    @Transactional
    public AuthTokensDto login(String email, String password, String userAgent, String ipAddress) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserAccount ua = users.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (ua.getStatus() != UserStatus.ACTIVE) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "User is not active");
        }

        refreshTokens.revokeAllForUser(ua.getId(), Instant.now());

        String access = jwt.issueAccessToken(ua.getId(), ua.getEmail(), ua.getRole().name());
        String refreshRaw = newRefreshRaw();

        RefreshToken rt = new RefreshToken();
        rt.setUser(ua);
        rt.setTokenHash(hash(refreshRaw));
        rt.setIssuedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plusMillis(props.getRefreshToken().getExpiration()));
        rt.setUserAgent(userAgent);
        rt.setIpAddress(ipAddress);
        refreshTokens.save(rt);

        return AuthTokensDto.builder()
            .tokenType("Bearer")
            .accessToken(access)
            .expiresInSeconds(props.getExpiration() / 1000)
            .refreshToken(refreshRaw)
            .build();
    }

    @Transactional
    public AuthTokensDto refresh(String refreshTokenRaw, String userAgent, String ipAddress) {
        String tokenHash = hash(refreshTokenRaw);
        RefreshToken rt = refreshTokens.findByTokenHash(tokenHash)
            .orElseThrow(() -> new ApiProblemException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (rt.getRevokedAt() != null) {
            throw new ApiProblemException(HttpStatus.UNAUTHORIZED, "Refresh token revoked");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiProblemException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserAccount ua = rt.getUser();
        if (ua.getStatus() != UserStatus.ACTIVE) {
            throw new ApiProblemException(HttpStatus.FORBIDDEN, "User is not active");
        }

        // rotate
        rt.setRevokedAt(Instant.now());
        String newRefreshRaw = newRefreshRaw();
        rt.setReplacedByTokenHash(hash(newRefreshRaw));
        refreshTokens.save(rt);

        RefreshToken next = new RefreshToken();
        next.setUser(ua);
        next.setTokenHash(rt.getReplacedByTokenHash());
        next.setIssuedAt(Instant.now());
        next.setExpiresAt(Instant.now().plusMillis(props.getRefreshToken().getExpiration()));
        next.setUserAgent(userAgent);
        next.setIpAddress(ipAddress);
        refreshTokens.save(next);

        String access = jwt.issueAccessToken(ua.getId(), ua.getEmail(), ua.getRole().name());
        return AuthTokensDto.builder()
            .tokenType("Bearer")
            .accessToken(access)
            .expiresInSeconds(props.getExpiration() / 1000)
            .refreshToken(newRefreshRaw)
            .build();
    }

    @Transactional
    public void logout(String refreshTokenRaw) {
        String tokenHash = hash(refreshTokenRaw);
        refreshTokens.findByTokenHash(tokenHash).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) {
                rt.setRevokedAt(Instant.now());
                refreshTokens.save(rt);
            }
        });
    }

    private static String newRefreshRaw() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Hash error", e);
        }
    }
}


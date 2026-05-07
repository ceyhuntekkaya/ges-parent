package com.genixo.ges.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties props;
    private final Key signingKey;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(normalizeSecret(props.getSecretKey())));
    }

    public String issueAccessToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusMillis(props.getExpiration());
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .addClaims(Map.of(
                "email", email,
                "role", role
            ))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private static String normalizeSecret(String raw) {
        // allow plain strings in config by treating them as UTF-8 and base64-encoding when needed
        // if it's already base64, decoding will work; if not, we encode to base64 first.
        try {
            Decoders.BASE64.decode(raw);
            return raw;
        } catch (Exception ignored) {
            return java.util.Base64.getEncoder().encodeToString(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}


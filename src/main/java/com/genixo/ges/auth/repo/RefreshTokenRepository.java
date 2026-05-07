package com.genixo.ges.auth.repo;

import com.genixo.ges.auth.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
        update RefreshToken rt
           set rt.revokedAt = :now
         where rt.user.id = :userId
           and rt.revokedAt is null
        """)
    int revokeAllForUser(@Param("userId") UUID userId, @Param("now") Instant now);
}


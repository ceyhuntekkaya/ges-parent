package com.genixo.ges.auth.repo;

import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.auth.model.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    /**
     * Admin detay: {@code applicantProfile} LAZY + inverse {@code OneToOne} olduğu için
     * {@link #findById} yerine bu metod kullanılmalı (profil aynı round-trip'te yüklenir).
     */
    @Query("SELECT u FROM UserAccount u LEFT JOIN FETCH u.applicantProfile WHERE u.id = :id")
    Optional<UserAccount> findByIdWithApplicantProfile(@Param("id") UUID id);

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Arama desenini dışarıda {@code %...%} ve küçük harf olarak verin (e-posta veya ad-soyad).
     */
    @Query(
        value = "SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.applicantProfile p WHERE "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:textPattern IS NULL OR LOWER(u.email) LIKE :textPattern OR "
            + "LOWER(CONCAT(COALESCE(p.firstName,''), ' ', COALESCE(p.lastName,''))) LIKE :textPattern)",
        countQuery = "SELECT COUNT(DISTINCT u) FROM UserAccount u LEFT JOIN u.applicantProfile p WHERE "
            + "(:role IS NULL OR u.role = :role) AND "
            + "(:textPattern IS NULL OR LOWER(u.email) LIKE :textPattern OR "
            + "LOWER(CONCAT(COALESCE(p.firstName,''), ' ', COALESCE(p.lastName,''))) LIKE :textPattern)"
    )
    Page<UserAccount> searchForAdmin(
        @Param("role") UserRole role,
        @Param("textPattern") String textPattern,
        Pageable pageable
    );
}


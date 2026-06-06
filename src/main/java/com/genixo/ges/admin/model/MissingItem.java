package com.genixo.ges.admin.model;

import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "missing_items")
public class MissingItem extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String module; // LANGUAGE_CAMP / UNIVERSITY

    @Column(nullable = false)
    private UUID applicationId;

    private UUID relatedEntityId; // participantId / referenceLetterId / ...

    @Column(nullable = false, length = 128)
    private String itemKey; // e.g. "IELTS_RESULT", "PASSPORT_COPY", "BANK_STATEMENT"

    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MissingItemStatus status = MissingItemStatus.OPEN;

    private Instant openedAt;
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opened_by_user_id")
    private UserAccount openedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private UserAccount resolvedBy;
}


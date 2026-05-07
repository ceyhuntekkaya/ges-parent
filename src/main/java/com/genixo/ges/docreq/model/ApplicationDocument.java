package com.genixo.ges.docreq.model;

import com.genixo.ges.auth.model.UserAccount;
import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.storage.model.StoredFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "application_documents")
public class ApplicationDocument extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DocumentRequirementScope scope;

    @Column(nullable = false)
    private UUID applicationId;

    @Column
    private UUID relatedEntityId; // participantId / referenceLetterId vb.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id")
    private DocumentRequirement requirement;

    @Column(nullable = false, length = 128)
    private String requirementKey; // denormalized for quick lookups & historical stability

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApplicationDocumentStatus status = ApplicationDocumentStatus.UPLOADED;

    private Instant uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private UserAccount uploadedBy;

    @Column(columnDefinition = "text")
    private String reviewNote;
}


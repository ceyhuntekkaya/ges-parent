package com.genixo.ges.languagecamp.model;

import com.genixo.ges.common.jpa.BaseEntity;
import com.genixo.ges.university.model.UniversityApplicationTaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_camp_application_tasks")
public class LanguageCampApplicationTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private LanguageCampApplication application;

    @Column(nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false, length = 256)
    private String withWhom;

    @Column(nullable = false, columnDefinition = "text")
    private String whatToDo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UniversityApplicationTaskStatus status = UniversityApplicationTaskStatus.PENDING;

    @Column(length = 128)
    private String performedByUser;
}

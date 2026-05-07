package com.genixo.ges.university.model;

import com.genixo.ges.common.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "language_exam_results")
public class LanguageExamResult extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_application_id", nullable = false)
    private UniversityApplication application;

    @Column(nullable = false, length = 64)
    private String examName; // IELTS, TOEFL, TestDaF, DELF, ...

    @Column(length = 64)
    private String score; // string to support band formats

    private LocalDate examDate;
}


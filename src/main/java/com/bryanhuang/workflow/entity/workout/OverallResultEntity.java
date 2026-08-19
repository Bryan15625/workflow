package com.bryanhuang.workflow.entity.workout;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cohort_overall_result")
@Getter
@NoArgsConstructor
@Builder
public class OverallResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String basis;

    @Column(nullable = false)
    private int passedCount;

    @Column(nullable = false)
    private int failedCount;

    public OverallResultEntity(UUID id, String basis, int passedCount, int failedCount) {
        this.id = id;
        this.basis = basis;
        this.passedCount = passedCount;
        this.failedCount = failedCount;
    }
}
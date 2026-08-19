package com.bryanhuang.workflow.entity.workout;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cohort_metric_result")
@Getter
@NoArgsConstructor
@Builder
public class MetricResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String metricId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "passed_block_id")
    private PassedBlockEntity passed;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "failed_block_id")
    private FailedBlockEntity failed;

    public MetricResultEntity(
            UUID id,
            String metricId,
            String title,
            String description,
            PassedBlockEntity passed,
            FailedBlockEntity failed
    ) {
        this.id = id;
        this.metricId = metricId;
        this.title = title;
        this.description = description;
        this.passed = passed;
        this.failed = failed;
    }
}
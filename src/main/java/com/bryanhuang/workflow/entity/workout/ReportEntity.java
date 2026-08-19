package com.bryanhuang.workflow.entity.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "report_entity")
@Getter
@NoArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_execution_id", nullable = false, unique = true)
    private WorkflowExecutionEntity workflowExecutionEntity;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "overall_result_id", nullable = false)
    private OverallResultEntity overallResult;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "cohort_analysis_result_id")
    private List<MetricResultEntity> metrics;

    public ReportEntity(
            UUID id,
            WorkflowExecutionEntity workflowExecutionEntity,
            OverallResultEntity overallResult,
            List<MetricResultEntity> metrics
    ) {
        this.id = id;
        this.workflowExecutionEntity = workflowExecutionEntity;
        this.overallResult = overallResult;
        this.metrics = metrics;
    }
}
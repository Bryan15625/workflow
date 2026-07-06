package com.bryanhuang.workflow.entity;


import com.bryanhuang.workflow.model.JobStatus;
import com.bryanhuang.workflow.model.StepName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_step_execution")
@Getter
@NoArgsConstructor
@Builder
public class StepExecutionStatusEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecutionEntity workflowExecutionEntity;
    private Integer stepId;
    private StepName stepName;

    @Enumerated(EnumType.STRING)
    private JobStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<Integer> dependsOnStepIds;

    public StepExecutionStatusEntity(
            UUID id,
            WorkflowExecutionEntity workflowExecutionEntity,
            Integer stepId,
            StepName stepName,
            JobStatus status,
            Instant startedAt,
            Instant completedAt,
            List<Integer> dependsOnStepIds
    ) {
        this.id = id;
        this.workflowExecutionEntity = workflowExecutionEntity;
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.dependsOnStepIds = dependsOnStepIds;
    }
    public void start() {
        if (status != JobStatus.READY) {
            throw new IllegalStateException("Execution must be READY to start.");
        }

        status = JobStatus.RUNNING;
        startedAt = Instant.now();
    }

    public void fail() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to fail.");
        }

        status = JobStatus.FAILED;
        completedAt = Instant.now();
    }

    public void complete() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to complete.");
        }

        status = JobStatus.COMPLETED;
        completedAt = Instant.now();
    }

    public void terminate() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to terminate.");
        }

        status = JobStatus.TERMINATED;
        completedAt = Instant.now();
    }

    @JsonIgnore
    public boolean isTerminal() {
        return status == JobStatus.COMPLETED
                || status == JobStatus.FAILED
                || status == JobStatus.TERMINATED;
    }
}
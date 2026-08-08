package com.bryanhuang.workflow.entity;


import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_execution")
@Getter
@NoArgsConstructor
@Builder
public class WorkflowExecutionEntity {

    @Id
    private UUID workflowExecutionId;
    private UUID workflowId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

    @Column(length = 1000)
    private String errorMessage;

    public WorkflowExecutionEntity(
            UUID workflowExecutionId,
            UUID workflowId,
            JobStatus status,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            String errorMessage
    ) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
    }

    public void start() {
        if (status != JobStatus.READY) {
            throw new IllegalStateException("Execution must be READY to start.");
        }

        status = JobStatus.RUNNING;
        startedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to fail.");
        }

        status = JobStatus.FAILED;
        completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void complete() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to complete.");
        }

        status = JobStatus.COMPLETED;
        completedAt = Instant.now();
    }

    public void terminate() {
        if (status != JobStatus.RUNNING && status != JobStatus.PAUSED) {
            throw new IllegalStateException("Execution must be RUNNING or PAUSED to terminate.");
        }

        status = JobStatus.TERMINATED;
        completedAt = Instant.now();
    }

    public void pause() {
        if (status != JobStatus.RUNNING) {
            throw new IllegalStateException("Execution must be RUNNING to pause.");
        }

        status = JobStatus.PAUSED;
    }

    public void resume() {
        if (status != JobStatus.PAUSED) {
            throw new IllegalStateException("Execution must be PAUSED to resume.");
        }

        status = JobStatus.RUNNING;
    }

    @JsonIgnore
    public boolean isTerminal() {
        return status == JobStatus.COMPLETED
                || status == JobStatus.FAILED
                || status == JobStatus.TERMINATED
                || status == JobStatus.SKIPPED;
    }

}

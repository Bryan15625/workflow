package com.bryanhuang.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class StepExecutionStatus {
    private final Integer stepId;
    private final StepName stepName;
    private JobStatus status;
    private Instant startedAt;
    private Instant completedAt;

    public StepExecutionStatus(
            Integer stepId,
            StepName stepName,
            JobStatus status,
            Instant startedAt,
            Instant completedAt
    ) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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

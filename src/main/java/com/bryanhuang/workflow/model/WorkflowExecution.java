package com.bryanhuang.workflow.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class WorkflowExecution {
    private UUID workflowExecutionId;
    private UUID workflowId;
    private JobStatus status;
    private List<StepExecutionStatus> stepStatuses;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public WorkflowExecution(
            UUID workflowExecutionId,
            UUID workflowId,
            JobStatus status,
            List<StepExecutionStatus> stepStatuses,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
        this.status = status;
        this.stepStatuses = stepStatuses;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}

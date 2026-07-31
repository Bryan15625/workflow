package com.bryanhuang.workflow.model.workflow;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class WorkflowExecution {
    private UUID workflowExecutionId;
    private UUID workflowId;
    private JobStatus status;
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

    public WorkflowExecution(
            UUID workflowExecutionId,
            UUID workflowId,
            JobStatus status,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt
    ) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
}

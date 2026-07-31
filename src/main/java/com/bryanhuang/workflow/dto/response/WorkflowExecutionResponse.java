package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.dto.StepExecutionResponse;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class WorkflowExecutionResponse {
    private final UUID workflowExecutionId;
    private final UUID workflowId;
    private final JobStatus status;
    private List<StepExecutionResponse> stepStatuses;
    private final Instant createdAt;
    private final Instant startedAt;
    private final Instant completedAt;

    public WorkflowExecutionResponse(
            UUID workflowExecutionId,
            UUID workflowId,
            JobStatus status,
            List<StepExecutionResponse> stepStatuses,
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

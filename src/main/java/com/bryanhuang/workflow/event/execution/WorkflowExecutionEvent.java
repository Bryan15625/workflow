package com.bryanhuang.workflow.event.execution;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WorkflowExecutionEvent {
    private UUID workflowExecutionId;
    private UUID workflowId;

    public WorkflowExecutionEvent(
            UUID workflowExecutionId,
            UUID workflowId) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
    }
}

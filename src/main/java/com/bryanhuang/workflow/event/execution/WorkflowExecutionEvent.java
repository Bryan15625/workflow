package com.bryanhuang.workflow.event.execution;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
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

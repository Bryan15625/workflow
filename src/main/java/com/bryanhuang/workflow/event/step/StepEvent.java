package com.bryanhuang.workflow.event.step;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class StepEvent {
    private UUID workflowExecutionId;
    private UUID workflowId;
    private Integer stepId;

    public StepEvent(
            UUID workflowExecutionId,
            UUID workflowId,
            Integer stepId
    ) {
        this.workflowExecutionId = workflowExecutionId;
        this.workflowId = workflowId;
        this.stepId = stepId;
    }
}

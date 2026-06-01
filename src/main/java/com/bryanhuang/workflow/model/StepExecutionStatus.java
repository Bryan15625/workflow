package com.bryanhuang.workflow.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StepExecutionStatus {
    private Integer stepId;
    private StepName stepName;
    private JobStatus status;

    public StepExecutionStatus(
            Integer stepId,
            StepName stepName,
            JobStatus status
    ) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
    }
}

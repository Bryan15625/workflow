package com.bryanhuang.workflow.dto;

import com.bryanhuang.workflow.model.JobStatus;
import com.bryanhuang.workflow.model.StepName;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// TODO: decouple class variables from the model
public class StepExecutionResponse {
    private Integer stepId;
    private StepName stepName;
    private JobStatus status;

    public StepExecutionResponse(Integer stepId, StepName stepName, JobStatus status) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
    }
}

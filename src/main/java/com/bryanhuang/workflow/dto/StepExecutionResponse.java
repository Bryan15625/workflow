package com.bryanhuang.workflow.dto;

import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workflow.StepName;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
// TODO: decouple class variables from the model
public class StepExecutionResponse {
    private final UUID stepExecutionId;
    private Integer stepId;
    private StepName stepName;
    private JobStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<Integer> dependsOnStepIds;

    public StepExecutionResponse(
            UUID stepExecutionId,
            Integer stepId,
            StepName stepName,
            JobStatus status,
            Instant startedAt,
            Instant completedAt,
            List<Integer> dependsOnStepIds
    ) {
        this.stepExecutionId = stepExecutionId;
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.dependsOnStepIds = dependsOnStepIds;
    }
}

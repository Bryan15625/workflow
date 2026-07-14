package com.bryanhuang.workflow.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class StepExecutionStatus {
    private final UUID id;
    private final Integer stepId;
    private final StepName stepName;
    private JobStatus status;
    private Instant startedAt;
    private Instant completedAt;
    private List<Integer> dependsOnStepIds;

    public StepExecutionStatus(
            UUID id,
            Integer stepId,
            StepName stepName,
            JobStatus status,
            Instant startedAt,
            Instant completedAt,
            List<Integer> dependsOnStepIds
    ) {
        this.id = id;
        this.stepId = stepId;
        this.stepName = stepName;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.dependsOnStepIds = dependsOnStepIds;
    }

}

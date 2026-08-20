package com.bryanhuang.workflow.model.workflow;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
public class Step {
    private Integer stepId;
    private StepName stepName;
    private List<Integer> dependsOnStepIds;

    public Step(Integer stepId, StepName stepName, List<Integer> dependsOnStepIds) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.dependsOnStepIds = dependsOnStepIds;
    }
}

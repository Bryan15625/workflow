package com.bryanhuang.workflow.dto;

import com.bryanhuang.workflow.model.workflow.StepName;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StepDto {
    @NotNull(message = "Step ID is required and must be an Integer")
    private Integer stepId;

    @NotNull(message = "Step name is required and cannot be blank")
    private StepName stepName;

    @NotNull(message = "Dependent IDs are required and cannot be blank")
    private List<Integer> dependsOnStepIds;

    public StepDto(
            Integer stepId,
            StepName stepName,
            List<Integer> dependsOnStepIds
    ) {
        this.stepId = stepId;
        this.stepName = stepName;
        this.dependsOnStepIds = dependsOnStepIds;
    }
}

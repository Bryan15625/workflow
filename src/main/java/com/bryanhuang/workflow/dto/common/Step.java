package com.bryanhuang.workflow.dto.common;

import com.bryanhuang.workflow.model.StepName;

import java.util.List;

public class Step {
    private Integer stepId;
    private StepName stepName;
    private List<Integer> dependsOnStepIds;
}

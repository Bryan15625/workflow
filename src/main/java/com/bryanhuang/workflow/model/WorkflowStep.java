package com.bryanhuang.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowStep {
    private Integer stepId;
    private String stepName;
    private List<Integer> nextStepIds;
}

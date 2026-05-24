package com.bryanhuang.workflow.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Step {
    private Integer stepId;
    private String stepName;
    private Integer[] nextStepIds;
}

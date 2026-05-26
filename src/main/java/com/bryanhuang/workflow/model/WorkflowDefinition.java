package com.bryanhuang.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDefinition {
    private UUID workflowDefinitionId;
    private String workflowDefinitionName;
    private Profile profile;
    private WorkflowInput input;
    private List<WorkflowStep> steps;
}

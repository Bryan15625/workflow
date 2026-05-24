package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.request.CreateWorkflowDefinitionRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowDefinitionResponse;
import com.bryanhuang.workflow.exception.CycleDetectedException;
import com.bryanhuang.workflow.exception.InvalidWorkflowDefinitionException;
import com.bryanhuang.workflow.model.WorkflowDefinition;
import com.bryanhuang.workflow.model.WorkflowStep;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WorkflowDefinitionService {

    public CreateWorkflowDefinitionResponse createWorkflowDefinition(CreateWorkflowDefinitionRequest request) {
        UUID workflowId = UUID.randomUUID();

        WorkflowDefinition workflowDefinition = new WorkflowDefinition(
                workflowId,
                request.getWorkflowDefinitionName(),
                request.getSteps().stream()
                        .map(step -> new WorkflowStep(
                                step.getStepId(),
                                step.getStepName(),
                                step.getNextStepIds()
                        ))
                        .toList()
        );
        // Validate workflow name is available
        if (!validateWorkflowName(workflowDefinition.getWorkflowDefinitionName())) {
            throw new InvalidWorkflowDefinitionException("Workflow name already exists");
        }
        // Validate no cycles in workflow definition
        if (detectCycle(workflowDefinition)) {
            throw new CycleDetectedException("Cycle detected in workflow definition");
        }
        // Save workflow definition to db
        saveWorkflowDefinition(workflowDefinition);

        return new CreateWorkflowDefinitionResponse(workflowId);
    }

    private boolean detectCycle(WorkflowDefinition workflowDefinition) {
        return false;
    }

    // TODO: Implement workflow name validation against db
    private boolean validateWorkflowName(String workflowName) {
        return true;
    }

    // TODO: Implement saving workflow definition to db
    private void saveWorkflowDefinition(WorkflowDefinition workflowDefinition) {
    }
}

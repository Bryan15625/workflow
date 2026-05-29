package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowDefinitionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.exception.InvalidWorkflowDefinitionException;
import com.bryanhuang.workflow.model.WorkflowInput;
import com.bryanhuang.workflow.model.Profile;
import com.bryanhuang.workflow.model.WorkflowDefinition;
import com.bryanhuang.workflow.model.WorkflowStep;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowService {

    public CreateWorkflowDefinitionResponse createWorkflowDefinition(CreateWorkflowRequest request) {
        UUID workflowId = UUID.randomUUID();

        WorkflowDefinition workflowDefinition = new WorkflowDefinition(
                workflowId,
                request.getWorkflowDefinitionName(),
                new Profile(
                        request.getProfile().getAge(),
                        request.getProfile().getWeightKg(),
                        request.getProfile().getHeightCm(),
                        request.getProfile().getSex(),
                        request.getProfile().getGoal()
                ),
                new WorkflowInput(
                        request.getInput().getSourceFilePath(),
                        request.getInput().getResultFilePath()
                ),
                request.getSteps().stream()
                        .map(step -> new WorkflowStep(
                                step.getStepId(),
                                step.getStepName(),
                                step.getDependsOnStepIds()
                        ))
                        .toList()
        );

        validateWorkflowDefinition(workflowDefinition);

        saveWorkflowDefinition(workflowDefinition);

        return new CreateWorkflowDefinitionResponse(workflowId);
    }

    public WorkflowResponse getWorkflowDefinition(UUID workflowId) {
        return null;
    }


    private void validateWorkflowDefinition(WorkflowDefinition workflowDefinition) {
        validateDag(workflowDefinition);

        // Ensure workflow name isn't already used in the DB
        validateWorkflowName(workflowDefinition.getWorkflowDefinitionName());

    }

    /*
    1. Step IDs are unique
    2. Every dependency ID exists
    3. There is at least one root step
    4. There is at least one terminal step
    5. No cycles exist
    6. No duplicate dependencies per step
     */
    private void validateDag(WorkflowDefinition workflowDefinition) {
        Set<Integer> stepIds = validateUniqueStepIds(workflowDefinition);
        validateDependencyIdsExist(workflowDefinition, stepIds);
        validateAtLeastOneRootStep(workflowDefinition);
        validateAtLeastOneTerminalStep(workflowDefinition);
        detectCycle(workflowDefinition);
    }

    private Set<Integer> validateUniqueStepIds(WorkflowDefinition workflowDefinition) {
        Set<Integer> stepIds = new HashSet<>();
        for (WorkflowStep step : workflowDefinition.getSteps()) {
            if (stepIds.contains(step.getStepId())) {
                throw new InvalidWorkflowDefinitionException("Duplicate step ID found");
            }
            stepIds.add(step.getStepId());
        }
        return stepIds;
    }

    private void validateDependencyIdsExist(WorkflowDefinition workflowDefinition, Set<Integer> stepIds) {
        for (WorkflowStep step : workflowDefinition.getSteps()) {
            for (Integer dependencyId : step.getDependsOnStepIds()) {
                if (!stepIds.contains(dependencyId)) {
                    throw new InvalidWorkflowDefinitionException(
                            "Step " + step.getStepId() + " depends on unknown step ID: " + dependencyId
                    );
                }
            }
        }
    }

    private void validateAtLeastOneRootStep(WorkflowDefinition workflowDefinition) {
        boolean hasRootStep = workflowDefinition.getSteps().stream()
                .anyMatch(step -> step.getDependsOnStepIds().isEmpty());

        if (!hasRootStep) {
            throw new InvalidWorkflowDefinitionException(
                    "Workflow must contain at least one root step with no dependencies"
            );
        }
    }

    private void validateAtLeastOneTerminalStep(WorkflowDefinition workflowDefinition) {
        Set<Integer> dependencyIds = workflowDefinition.getSteps().stream()
                .flatMap(step -> step.getDependsOnStepIds().stream())
                .collect(java.util.stream.Collectors.toSet());

        boolean hasTerminalStep = workflowDefinition.getSteps().stream()
                .anyMatch(step -> !dependencyIds.contains(step.getStepId()));

        if (!hasTerminalStep) {
            throw new InvalidWorkflowDefinitionException(
                    "Workflow must contain at least one terminal step"
            );
        }
    }

    private void detectCycle(WorkflowDefinition workflowDefinition) {
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

        for (WorkflowStep step : workflowDefinition.getSteps()) {
            adjacencyList.putIfAbsent(step.getStepId(), new java.util.ArrayList<>());

            for (Integer dependencyId : step.getDependsOnStepIds()) {
                adjacencyList.putIfAbsent(dependencyId, new java.util.ArrayList<>());
                adjacencyList.get(dependencyId).add(step.getStepId());
            }
        }

        Set<Integer> visiting = new HashSet<>();
        Set<Integer> visited = new HashSet<>();

        for (Integer stepId : adjacencyList.keySet()) {
            if (hasCycle(stepId, adjacencyList, visiting, visited)) {
                throw new InvalidWorkflowDefinitionException("Cycle detected in workflow definition");
            }
        }

    }

    private boolean hasCycle(
            Integer stepId,
            Map<Integer, List<Integer>> adjacencyList,
            @NonNull Set<Integer> visiting,
            Set<Integer> visited
    ) {
        if (visiting.contains(stepId)) {
            return true;
        }

        if (visited.contains(stepId)) {
            return false;
        }

        visiting.add(stepId);

        for (Integer nextStepId : adjacencyList.getOrDefault(stepId, List.of())) {
            if (hasCycle(nextStepId, adjacencyList, visiting, visited)) {
                return true;
            }
        }

        visiting.remove(stepId);
        visited.add(stepId);

        return false;
    }

    // TODO: Implement workflow name validation against db
    private void validateWorkflowName(String workflowName) {

    }

    // TODO: Implement saving workflow definition to db
    private void saveWorkflowDefinition(WorkflowDefinition workflowDefinition) {
    }
}

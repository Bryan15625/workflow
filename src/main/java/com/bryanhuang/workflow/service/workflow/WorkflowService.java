package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.entity.workflow.WorkflowEntity;
import com.bryanhuang.workflow.exception.CycleDetectedException;
import com.bryanhuang.workflow.exception.InvalidWorkflowException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowMapper;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowMapper workflowMapper;
    private final WorkflowEntityMapper workflowEntityMapper;
    private final WorkflowQueryService workflowQueryService;
    private final WorkflowRepository workflowRepository;

    public CreateWorkflowResponse createWorkflow(CreateWorkflowRequest request) {
        UUID workflowId = UUID.randomUUID();

        Workflow workflow = workflowMapper.toWorkflow(workflowId, request);
        validateWorkflow(workflow);
        WorkflowEntity workflowEntity = workflowEntityMapper.toWorkflowEntity(workflow);
        WorkflowEntity saved = saveWorkflow(workflowEntity);

        return new CreateWorkflowResponse(workflowId);
    }

    public WorkflowResponse getWorkflow(UUID workflowId) {
        Workflow workflow = workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId);
        return workflowMapper.toWorkflowResponse(workflow);
    }

    private WorkflowEntity saveWorkflow(WorkflowEntity entity) {
        String name = entity.getWorkflowName();
        if (workflowRepository.findByWorkflowName(name).isPresent()) {
            throw new InvalidWorkflowException("Workflow name already exists");
        }

        return workflowRepository.save(entity);

    }


    private void validateWorkflow(Workflow workflow) {
        validateDag(workflow);
    }


    /**
     *  1. Step IDs are unique
     *  2. Every dependency ID exists
     *  3. There is at least one root step
     *  4. There is at least one terminal step
     *  5. No cycles exist
     *  6. No duplicate dependencies per step
     * @param workflow
     */
    private void validateDag(Workflow workflow) {
        Set<Integer> stepIds = validateUniqueStepIds(workflow);
        validateDependencyIdsExist(workflow, stepIds);
        validateAtLeastOneRootStep(workflow);
        validateAtLeastOneTerminalStep(workflow);
        detectCycle(workflow);
    }

    private Set<Integer> validateUniqueStepIds(Workflow workflow) {
        Set<Integer> stepIds = new HashSet<>();
        for (Step step : workflow.getSteps()) {
            if (stepIds.contains(step.getStepId())) {
                throw new InvalidWorkflowException("Duplicate step ID found");
            }
            stepIds.add(step.getStepId());
        }
        return stepIds;
    }

    private void validateDependencyIdsExist(Workflow workflow, Set<Integer> stepIds) {
        for (Step step : workflow.getSteps()) {
            for (Integer dependencyId : step.getDependsOnStepIds()) {
                if (!stepIds.contains(dependencyId)) {
                    throw new InvalidWorkflowException(
                            "Step " + step.getStepId() + " depends on unknown step ID: " + dependencyId
                    );
                }
            }
        }
    }

    private void validateAtLeastOneRootStep(Workflow workflow) {
        boolean hasRootStep = workflow.getSteps().stream()
                .anyMatch(step -> step.getDependsOnStepIds().isEmpty());

        if (!hasRootStep) {
            throw new InvalidWorkflowException(
                    "Workflow must contain at least one root step with no dependencies"
            );
        }
    }

    private void validateAtLeastOneTerminalStep(Workflow workflow) {
        Set<Integer> dependencyIds = workflow.getSteps().stream()
                .flatMap(step -> step.getDependsOnStepIds().stream())
                .collect(java.util.stream.Collectors.toSet());

        boolean hasTerminalStep = workflow.getSteps().stream()
                .anyMatch(step -> !dependencyIds.contains(step.getStepId()));

        if (!hasTerminalStep) {
            throw new InvalidWorkflowException(
                    "Workflow must contain at least one terminal step"
            );
        }
    }

    private void detectCycle(Workflow workflow) {
        Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

        for (Step step : workflow.getSteps()) {
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
                throw new CycleDetectedException("Cycle detected in workflow");
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

}

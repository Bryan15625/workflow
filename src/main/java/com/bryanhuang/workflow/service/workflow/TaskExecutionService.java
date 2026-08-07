package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;

import com.bryanhuang.workflow.exception.InvalidWorkflowException;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionService {
    private final WorkflowQueryService workflowQueryService;
    private final StepExecutionService stepExecutionService;
    private final WorkflowExecutionService workflowExecutionService;
    private final StepExecutorService stepExecutorService;
    private final WorkflowControlGate workflowControlGate;
    private final WorkflowCleanupService workflowCleanupService;

    public void executeWorkflow(WorkflowExecutionEntity entity) {
        log.info("Executing workflow: {}", entity.getWorkflowId());
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        try {
            workflowExecutionService.start(workflowExecutionId);
            Workflow workflow = workflowQueryService
                    .findWorkflowEntityAndMapToWorkflow(entity.getWorkflowId());

            List<Step> orderedSteps = buildExecutionOrder(workflow.getSteps());
            for (Step step : orderedSteps) {
                if (workflowControlGate.checkpoint(workflowExecutionId) == JobControl.TERMINATE) {
                    log.info("Workflow execution terminated: {}", workflowExecutionId);
                    return;
                }
                executeStep(entity, step, workflow);
            }

            workflowExecutionService.complete(workflowExecutionId);
            log.info("Task execution completed");
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            try {
                workflowExecutionService.fail(workflowExecutionId, e.getMessage());
                workflowCleanupService.cleanupWorkflowExecution(workflowExecutionId);
            } catch (IllegalStateException ignored) {
                log.warn(
                        "Unable to mark workflow as failed because state already changed for " +
                                "workflowExecutionId={}",
                        workflowExecutionId
                );
            }
        }
    }


    private void executeStep(WorkflowExecutionEntity entity, Step step, Workflow workflow)
            throws InterruptedException {
        try {
            log.info("Executing step {}", step.getStepId());
            stepExecutionService.start(entity.getWorkflowExecutionId(), step.getStepId());
            stepExecutorService.execute(step, workflow, entity);
            stepExecutionService.complete(entity.getWorkflowExecutionId(), step.getStepId());
        } catch (Exception e) {
            log.error("Step {} failed", step.getStepId(), e);
            stepExecutionService.fail(entity.getWorkflowExecutionId(), step.getStepId());
            throw e;
        }
    }

    public List<Step> buildExecutionOrder(List<Step> steps) {
        Map<Integer, Step> byId = steps.stream()
                .collect(toMap(Step::getStepId, s -> s));

        Map<Integer, Step> remaining = new HashMap<>(byId);
        Set<Integer> completed = new HashSet<>();
        List<Step> order = new ArrayList<>();

        while (!remaining.isEmpty()) {
            List<Integer> readyIds = remaining.values().stream()
                    .filter(step -> completed.containsAll(step.getDependsOnStepIds()))
                    .map(Step::getStepId)
                    .toList();

            if (readyIds.isEmpty()) {
                throw new InvalidWorkflowException("No executable step found. " +
                        "Dependency graph is invalid.");
            }

            for (Integer id : readyIds) {
                Step step = remaining.remove(id);
                order.add(step);
                completed.add(id);
            }
        }

        return order;
    }
}

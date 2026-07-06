package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;

import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskExecutionService {
    private final WorkflowQueryService workflowQueryService;
    private final StepExecutionService stepExecutionService;
    private final WorkflowExecutionService workflowExecutionService;

    public void executeWorkflow(WorkflowExecutionEntity entity) {
        log.info("Executing workflow");
        try {
            workflowExecutionService.start(entity.getWorkflowExecutionId());
            Workflow workflow = workflowQueryService.findWorkflow(entity.getWorkflowId());
            for (Step step : workflow.getSteps()) {
                executeStep(entity.getWorkflowExecutionId(), step.getStepId());
            }
            workflowExecutionService.complete(entity.getWorkflowExecutionId());
            log.info("Task execution completed");
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            entity.fail();
        }
    }


    public void executeStep(UUID workflowExecutionId, Integer stepId) {
        try {
            log.info("Executing step {}", stepId);
            stepExecutionService.start(workflowExecutionId, stepId);
            Thread.sleep(10000);
            stepExecutionService.complete(workflowExecutionId, stepId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}

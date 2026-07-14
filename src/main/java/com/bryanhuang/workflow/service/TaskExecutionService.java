package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;

import com.bryanhuang.workflow.model.JobControl;
import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
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
    private final WorkflowExecutionRedisService workflowExecutionRedisService;

    public void executeWorkflow(WorkflowExecutionEntity entity) {
        log.info("Executing workflow");
        UUID workflowExecutionId = entity.getWorkflowExecutionId();
        try {
            workflowExecutionService.start(workflowExecutionId);
            Workflow workflow = workflowQueryService
                    .findWorkflowEntityAndMapToWorkflow(entity.getWorkflowId());

            for (Step step : workflow.getSteps()) {
                if (handleWorkflowControl(workflowExecutionId) == JobControl.TERMINATE) {
                    log.info("Workflow execution terminated: {}", workflowExecutionId);
                    return;
                }
                executeStep(workflowExecutionId, step.getStepId());
            }

            workflowExecutionService.complete(workflowExecutionId);
            log.info("Task execution completed");
        } catch (Exception e) {
            log.error("Workflow execution failed", e);
            try {
                workflowExecutionService.fail(workflowExecutionId);
            } catch (IllegalStateException ignored) {
                log.warn(
                        "Unable to mark workflow as failed because state already changed for workflowExecutionId={}",
                        workflowExecutionId
                );
            }
        }
    }


    private void executeStep(UUID workflowExecutionId, Integer stepId) throws InterruptedException {
        try {
            log.info("Executing step {}", stepId);
            stepExecutionService.start(workflowExecutionId, stepId);
            Thread.sleep(10000);
            stepExecutionService.complete(workflowExecutionId, stepId);
        } catch (Exception e) {
            log.error("Step {} failed", stepId, e);
            stepExecutionService.fail(workflowExecutionId, stepId);
            throw e;
        }
    }

    private JobControl handleWorkflowControl(UUID workflowExecutionId) throws InterruptedException {
        JobControl control = workflowExecutionRedisService.getControl(workflowExecutionId);
        if (control == null) {
            return JobControl.NONE;
        }
        else if (control == JobControl.PAUSE) {
            workflowExecutionService.pause(workflowExecutionId);
            log.info("Workflow execution paused");

            while (true) {
                Thread.sleep(1000);
                control = workflowExecutionRedisService.getControl(workflowExecutionId);
                if (control == null) {
                    continue;
                }
                switch (control) {
                    case RESUME -> {
                        workflowExecutionService.resume(workflowExecutionId);
                        return JobControl.RESUME;
                    }
                    case TERMINATE -> {
                        terminateWorkflow(workflowExecutionId);
                        return JobControl.TERMINATE;
                    }
                }
            }
        }
        else if (control == JobControl.TERMINATE) {
            terminateWorkflow(workflowExecutionId);
            return JobControl.TERMINATE;
        }
        return control;
    }

    public void terminateWorkflow(UUID workflowExecutionId) {
        log.info("Terminating workflow");
        workflowExecutionService.terminate(workflowExecutionId);
    }
}

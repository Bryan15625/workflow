package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowControlGate {

    private final WorkflowExecutionRedisService workflowExecutionRedisService;
    private final WorkflowExecutionService workflowExecutionService;
    private final WorkflowCleanupService workflowCleanupService;
    private final StepExecutionService stepExecutionService;

    // Checkpoint between steps
    public JobControl checkpoint(UUID workflowExecutionId) throws InterruptedException {
        JobControl control = workflowExecutionRedisService.getControl(workflowExecutionId);
        if (control == null) {
            return JobControl.NONE;
        }
        if (control == JobControl.PAUSE) {
            workflowExecutionService.pause(workflowExecutionId);
            log.info("Workflow execution paused between steps: {}", workflowExecutionId);

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
                    default -> { /* still paused, keep waiting */ }
                }
            }
        }
        if (control == JobControl.TERMINATE) {
            terminateWorkflow(workflowExecutionId);
            return JobControl.TERMINATE;
        }
        return control;
    }

    // Checkpoint mid-step execution
    public JobControl checkpointStep(UUID workflowExecutionId, Integer stepId) throws InterruptedException {
        JobControl control = workflowExecutionRedisService.getControl(workflowExecutionId);
        if (control == null) {
            return JobControl.NONE;
        }
        else if (control == JobControl.PAUSE) {
            workflowExecutionService.pause(workflowExecutionId);
            stepExecutionService.pause(workflowExecutionId, stepId);
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
                        stepExecutionService.resume(workflowExecutionId, stepId);
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

    private void terminateWorkflow(UUID workflowExecutionId) {
        log.info("Terminating workflow");
        try {
            workflowExecutionService.terminate(workflowExecutionId);
        } catch (IllegalStateException ignored) {
            log.warn("Workflow already in terminal state for workflowExecutionId={}", workflowExecutionId);
        }
        stepExecutionService.terminateAll(workflowExecutionId);
        workflowCleanupService.cleanupWorkflowExecution(workflowExecutionId);
    }
}

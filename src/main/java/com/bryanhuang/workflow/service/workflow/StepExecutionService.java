package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.StepExecutionStatusEntity;
import com.bryanhuang.workflow.exception.StepExecutionStatusNotFoundException;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StepExecutionService {
    private final StepExecutionStatusRepository stepExecutionStatusRepository;

    @Transactional
    public void start(UUID workflowExecutionId, Integer stepId) {
        StepExecutionStatusEntity step =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(
                                workflowExecutionId,
                                stepId
                        )
                        .orElseThrow(
                                () -> new StepExecutionStatusNotFoundException("Step status not found")
                        );

        step.start();
    }
    @Transactional
    public void complete(UUID workflowExecutionId, Integer stepId) {
        StepExecutionStatusEntity step =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(
                                workflowExecutionId,
                                stepId
                        )
                        .orElseThrow(
                                () -> new StepExecutionStatusNotFoundException("Step status not found")
                        );

        step.complete();
    }

    @Transactional
    public void pause(UUID workflowExecutionId, Integer stepId) {
        StepExecutionStatusEntity step =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(
                                workflowExecutionId,
                                stepId
                        )
                        .orElseThrow(
                                () -> new StepExecutionStatusNotFoundException("Step status not found")
                        );
        step.pause();
    }

    @Transactional
    public void resume(UUID workflowExecutionId, Integer stepId) {
        StepExecutionStatusEntity step =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(
                                workflowExecutionId,
                                stepId
                        )
                        .orElseThrow(
                                () -> new StepExecutionStatusNotFoundException("Step status not found")
                        );
        step.resume();
    }


    @Transactional
    public void fail(UUID workflowExecutionId, Integer stepId) {
        log.info("failing the step now");
        List<StepExecutionStatusEntity> steps =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(
                                workflowExecutionId
                        );
        boolean markFailed = false;
        for (StepExecutionStatusEntity step : steps) {
            if (Objects.equals(step.getStepId(), stepId)) {
                markFailed = true;
                step.fail();
            } else if (step.getStatus() == JobStatus.READY) {
                step.markSkipped();
            }
        }
        if (!markFailed) {
            throw new StepExecutionStatusNotFoundException("Step status to fail not found");
        }
    }

    @Transactional
    public void terminateAll(UUID workflowExecutionId) {
        List<StepExecutionStatusEntity> steps =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(
                                workflowExecutionId
                        );
        for (StepExecutionStatusEntity step : steps) {
            if (step.getStatus() == JobStatus.RUNNING || step.getStatus() == JobStatus.PAUSED) {
                step.markTerminated();
            } else if (step.getStatus() == JobStatus.READY) {
                 step.markSkipped();
            }
        }
    }
}

package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.exception.StepExecutionStatusNotFoundException;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    public void fail(UUID workflowExecutionId, Integer stepId) {
        StepExecutionStatusEntity step =
                stepExecutionStatusRepository
                        .findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(
                                workflowExecutionId,
                                stepId
                        )
                        .orElseThrow(
                                () -> new StepExecutionStatusNotFoundException("Step status not found")
                        );
        step.fail();
    }
}

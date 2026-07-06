package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionWorker {

    private final TaskExecutionService taskExecutionService;
    private final WorkflowQueryService workflowQueryService;

    @Async
    public void executeWorkflow(UUID executionId) {
        WorkflowExecutionEntity entity = workflowQueryService
                .getWorkflowExecutionEntityById(executionId);
        taskExecutionService.executeWorkflow(entity);
    }

}

package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionWorker {

    private final TaskExecutionService taskExecutionService;
    private final WorkflowExecutionRepository workflowExecutionRepository;

    @Async
    public void executeWorkflow(UUID executionId) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findById(executionId)
                .orElseThrow(() -> new WorkflowExecutionNotFoundException(
                        "Workflow execution not found with id: " + executionId
                ));
        taskExecutionService.executeWorkflow(entity);
    }

}

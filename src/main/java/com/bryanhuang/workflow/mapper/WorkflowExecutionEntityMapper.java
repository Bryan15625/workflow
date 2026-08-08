package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.WorkflowExecution;
import org.springframework.stereotype.Component;

@Component
public class WorkflowExecutionEntityMapper {

    public WorkflowExecutionEntity toWorkflowExecutionEntity(WorkflowExecution workflowExecution) {

        return WorkflowExecutionEntity.builder()
                .workflowExecutionId(workflowExecution.getWorkflowExecutionId())
                .workflowId(workflowExecution.getWorkflowId())
                .status(workflowExecution.getStatus())
                .createdAt(workflowExecution.getCreatedAt())
                .startedAt(workflowExecution.getStartedAt())
                .completedAt(workflowExecution.getCompletedAt())
                .errorMessage(workflowExecution.getErrorMessage())
                .build();
    }

    public WorkflowExecution toWorkflowExecution(WorkflowExecutionEntity workflowExecutionEntity) {

        return WorkflowExecution.builder()
                .workflowExecutionId(workflowExecutionEntity.getWorkflowExecutionId())
                .workflowId(workflowExecutionEntity.getWorkflowId())
                .status(workflowExecutionEntity.getStatus())
                .createdAt(workflowExecutionEntity.getCreatedAt())
                .startedAt(workflowExecutionEntity.getStartedAt())
                .completedAt(workflowExecutionEntity.getCompletedAt())
                .errorMessage(workflowExecutionEntity.getErrorMessage())
                .build();
    }

}

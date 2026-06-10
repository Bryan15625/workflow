package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.WorkflowExecution;
import com.bryanhuang.workflow.entity.payload.WorkflowExecutionPayload;
import org.springframework.stereotype.Component;

@Component
public class WorkflowExecutionEntityMapper {

    public WorkflowExecutionEntity toWorkflowExecutionEntity(WorkflowExecution workflowExecution) {

        WorkflowExecutionPayload workflowExecutionPayload = new WorkflowExecutionPayload(workflowExecution.getStepStatuses());
        return WorkflowExecutionEntity.builder()
                .workflowExecutionId(workflowExecution.getWorkflowExecutionId())
                .workflowId(workflowExecution.getWorkflowId())
                .status(workflowExecution.getStatus())
                .executionPayload(workflowExecutionPayload)
                .createdAt(workflowExecution.getCreatedAt())
                .startedAt(workflowExecution.getStartedAt())
                .completedAt(workflowExecution.getCompletedAt())
                .build();
    }

    public WorkflowExecution toWorkflowExecution(WorkflowExecutionEntity workflowExecutionEntity) {

        return WorkflowExecution.builder()
                .workflowExecutionId(workflowExecutionEntity.getWorkflowExecutionId())
                .workflowId(workflowExecutionEntity.getWorkflowId())
                .status(workflowExecutionEntity.getStatus())
                .stepStatuses(workflowExecutionEntity.getExecutionPayload().stepStatuses())
                .createdAt(workflowExecutionEntity.getCreatedAt())
                .startedAt(workflowExecutionEntity.getStartedAt())
                .completedAt(workflowExecutionEntity.getCompletedAt())
                .build();
    }

}

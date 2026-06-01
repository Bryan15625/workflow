package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.StepExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.model.WorkflowExecution;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkflowExecutionMapper {

    public WorkflowExecutionResponse toWorkflowExecutionResponse(
            WorkflowExecution workflowExecution
    ) {
        List<StepExecutionResponse> stepExecutionResponse = workflowExecution
                .getStepStatuses().stream()
                .map(stepStatus -> StepExecutionResponse.builder()
                        .stepId(stepStatus.getStepId())
                        .stepName(stepStatus.getStepName())
                        .status(stepStatus.getStatus())
                        .build())
                .toList();

        return WorkflowExecutionResponse.builder()
                .workflowExecutionId(workflowExecution.getWorkflowExecutionId())
                .workflowId(workflowExecution.getWorkflowId())
                .status(workflowExecution.getStatus())
                .stepStatuses(stepExecutionResponse)
                .createdAt(workflowExecution.getCreatedAt())
                .startedAt(workflowExecution.getStartedAt())
                .completedAt(workflowExecution.getCompletedAt())
                .build();
    }

}

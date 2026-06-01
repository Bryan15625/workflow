package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.mapper.WorkflowExecutionEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowExecutionMapper;
import com.bryanhuang.workflow.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {

    private final WorkflowExecutionEntityMapper workflowExecutionEntityMapper;
    private final WorkflowQueryService workflowQueryService;
    private final WorkflowExecutionMapper workflowExecutionMapper;

    public WorkflowExecutionResponse executeWorkflow(UUID workflowId) {

        WorkflowExecution workflowExecution = createWorkflowExecution(workflowId);

        WorkflowExecutionEntity entity = workflowExecutionEntityMapper
                .toWorkflowExecutionEntity(workflowExecution);

        WorkflowExecutionEntity saved = workflowQueryService
                .saveWorkflowExecutionEntity(entity);

        WorkflowExecution persisted = workflowExecutionEntityMapper.toWorkflowExecution(saved);

        return workflowExecutionMapper.toWorkflowExecutionResponse(persisted);
    }

    public WorkflowExecutionResponse getWorkflowExecutionStatus(UUID executionId) {
        return null;
    }

    public WorkflowExecution createWorkflowExecution(UUID workflowId) {
        UUID executionId = UUID.randomUUID();
        List<Step> steps = workflowQueryService.findWorkflow(workflowId).getSteps();

        List<StepExecutionStatus> stepStatuses = steps.stream()
                .map(step -> StepExecutionStatus.builder()
                        .stepId(step.getStepId())
                        .stepName(step.getStepName())
                        .status(JobStatus.READY)
                        .build())
                .toList();

        return WorkflowExecution.builder()
                .workflowExecutionId(executionId)
                .workflowId(workflowId)
                .status(JobStatus.READY)
                .stepStatuses(stepStatuses)
                .build();
    }
}

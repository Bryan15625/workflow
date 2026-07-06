package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.StepExecutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StepExecutionStatusEntityMapper {
    public StepExecutionStatusEntity toStepExecutionStatusEntity(
            StepExecutionStatus stepExecutionStatus,
            WorkflowExecutionEntity executionEntity) {

        return StepExecutionStatusEntity.builder()
                .id(stepExecutionStatus.getId())
                .workflowExecutionEntity(executionEntity)
                .stepId(stepExecutionStatus.getStepId())
                .stepName(stepExecutionStatus.getStepName())
                .status(stepExecutionStatus.getStatus())
                .startedAt(stepExecutionStatus.getStartedAt())
                .completedAt(stepExecutionStatus.getCompletedAt())
                .dependsOnStepIds(stepExecutionStatus.getDependsOnStepIds())
                .build();
    }

    public StepExecutionStatus toStepExecutionStatus(
            StepExecutionStatusEntity stepExecutionStatusEntity) {

        return StepExecutionStatus.builder()
                .id(stepExecutionStatusEntity.getId())
                .stepId(stepExecutionStatusEntity.getStepId())
                .stepName(stepExecutionStatusEntity.getStepName())
                .status(stepExecutionStatusEntity.getStatus())
                .startedAt(stepExecutionStatusEntity.getStartedAt())
                .completedAt(stepExecutionStatusEntity.getCompletedAt())
                .dependsOnStepIds(stepExecutionStatusEntity.getDependsOnStepIds())
                .build();
    }
}

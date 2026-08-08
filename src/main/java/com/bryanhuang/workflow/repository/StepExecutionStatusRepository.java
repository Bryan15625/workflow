package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StepExecutionStatusRepository extends JpaRepository<StepExecutionStatusEntity, UUID> {
    List<StepExecutionStatusEntity> findByWorkflowExecutionEntityOrderByStepIdAsc(WorkflowExecutionEntity workflowExecutionEntity);
    Optional<StepExecutionStatusEntity> findByWorkflowExecutionEntity_WorkflowExecutionIdAndStepId(UUID workflowExecutionId, Integer stepId);
    List<StepExecutionStatusEntity> findByWorkflowExecutionEntity_WorkflowExecutionIdOrderByStepIdAsc(
            UUID workflowExecutionId);
}
package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, UUID> {
    Optional<WorkflowExecutionEntity> findByWorkflowExecutionId(UUID workflowExecutionId);
}

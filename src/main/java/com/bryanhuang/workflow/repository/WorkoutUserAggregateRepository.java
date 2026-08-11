package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.WorkoutUserAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WorkoutUserAggregateRepository extends JpaRepository<WorkoutUserAggregateEntity, UUID> {

    @Modifying
    @Query("DELETE FROM WorkoutUserAggregateEntity w WHERE w.workflowExecutionEntity.workflowExecutionId = :workflowExecutionId")
    void deleteByWorkflowExecutionEntity_WorkflowExecutionId(@Param("workflowExecutionId") UUID workflowExecutionId);
}

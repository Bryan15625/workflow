package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.workout.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

    Optional<ReportEntity> findByWorkflowExecutionEntity_WorkflowExecutionId(UUID workflowExecutionId);

    @Modifying
    @Query("DELETE FROM WorkoutUserAggregateEntity w WHERE w.workflowExecutionEntity.workflowExecutionId = :workflowExecutionId")
    void deleteByWorkflowExecutionEntity_WorkflowExecutionId(@Param("workflowExecutionId") UUID workflowExecutionId);
}

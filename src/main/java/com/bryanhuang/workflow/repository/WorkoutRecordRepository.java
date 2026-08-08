package com.bryanhuang.workflow.repository;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.WorkoutRecordEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecordEntity, UUID> {
    Slice<WorkoutRecordEntity> findSliceByWorkflowExecutionEntity(WorkflowExecutionEntity workflowExecutionEntity, Pageable pageable);

    @Modifying
    @Query("DELETE FROM WorkoutRecordEntity w WHERE w.workflowExecutionEntity.workflowExecutionId = :workflowExecutionId")
    void deleteByWorkflowExecutionEntity_WorkflowExecutionId(@Param("workflowExecutionId") UUID workflowExecutionId);
}

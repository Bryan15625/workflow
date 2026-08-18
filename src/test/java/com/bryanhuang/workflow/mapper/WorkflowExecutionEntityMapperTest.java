package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workflow.WorkflowExecution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowExecutionEntityMapperTest {

    private final WorkflowExecutionEntityMapper mapper = new WorkflowExecutionEntityMapper();

    @Nested
    @DisplayName("toWorkflowExecutionEntity")
    class ToWorkflowExecutionEntityTests {

        @Test
        void toWorkflowExecutionEntity_shouldMapAllFields() {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();
            JobStatus status = JobStatus.RUNNING;
            Instant createdAt = Instant.now().minusSeconds(300);
            Instant startedAt = Instant.now().minusSeconds(200);
            Instant completedAt = Instant.now().minusSeconds(100);

            WorkflowExecution workflowExecution = WorkflowExecution.builder()
                    .workflowExecutionId(workflowExecutionId)
                    .workflowId(workflowId)
                    .status(status)
                    .createdAt(createdAt)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .build();

            WorkflowExecutionEntity entity = mapper.toWorkflowExecutionEntity(workflowExecution);

            assertEquals(workflowExecutionId, entity.getWorkflowExecutionId());
            assertEquals(workflowId, entity.getWorkflowId());
            assertEquals(status, entity.getStatus());
            assertEquals(createdAt, entity.getCreatedAt());
            assertEquals(startedAt, entity.getStartedAt());
            assertEquals(completedAt, entity.getCompletedAt());
        }
    }

    @Nested
    @DisplayName("toWorkflowExecution")
    class ToWorkflowExecutionTests {

        @Test
        void toWorkflowExecution_shouldMapAllFields() {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();
            JobStatus status = JobStatus.COMPLETED;
            Instant createdAt = Instant.now().minusSeconds(500);
            Instant startedAt = Instant.now().minusSeconds(400);
            Instant completedAt = Instant.now().minusSeconds(50);

            WorkflowExecutionEntity entity = WorkflowExecutionEntity.builder()
                    .workflowExecutionId(workflowExecutionId)
                    .workflowId(workflowId)
                    .status(status)
                    .createdAt(createdAt)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .build();

            WorkflowExecution workflowExecution = mapper.toWorkflowExecution(entity);

            assertEquals(workflowExecutionId, workflowExecution.getWorkflowExecutionId());
            assertEquals(workflowId, workflowExecution.getWorkflowId());
            assertEquals(status, workflowExecution.getStatus());
            assertEquals(createdAt, workflowExecution.getCreatedAt());
            assertEquals(startedAt, workflowExecution.getStartedAt());
            assertEquals(completedAt, workflowExecution.getCompletedAt());
        }
    }
}
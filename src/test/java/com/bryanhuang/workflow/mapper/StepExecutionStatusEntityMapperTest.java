package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workflow.StepExecutionStatus;
import com.bryanhuang.workflow.model.workflow.StepName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StepExecutionStatusEntityMapperTest {

    private final StepExecutionStatusEntityMapper mapper = new StepExecutionStatusEntityMapper();

    @Nested
    @DisplayName("toStepExecutionStatusEntity")
    class ToStepExecutionStatusEntityTests {

        @Test
        void toStepExecutionStatusEntity_shouldMapAllFields() {
            UUID id = UUID.randomUUID();
            Integer stepId = 1;
            StepName stepName = StepName.INGEST_CSV;
            JobStatus status = JobStatus.RUNNING;
            Instant startedAt = Instant.now().minusSeconds(60);
            Instant completedAt = Instant.now();
            List<Integer> dependsOnStepIds = List.of(2, 3);


            StepExecutionStatus stepExecutionStatus = StepExecutionStatus.builder()
                    .id(id)
                    .stepId(stepId)
                    .stepName(stepName)
                    .status(status)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .dependsOnStepIds(dependsOnStepIds)
                    .build();

            WorkflowExecutionEntity workflowExecutionEntity = WorkflowExecutionEntity.builder()
                    .workflowExecutionId(UUID.randomUUID())
                    .workflowId(UUID.randomUUID())
                    .status(JobStatus.READY)
                    .createdAt(Instant.now().minusSeconds(120))
                    .startedAt(null)
                    .completedAt(null)
                    .build();

            StepExecutionStatusEntity stepExecutionStatusEntity = mapper
                    .toStepExecutionStatusEntity(stepExecutionStatus, workflowExecutionEntity);

            assertEquals(id, stepExecutionStatusEntity.getId());
            assertEquals(stepId, stepExecutionStatusEntity.getStepId());
            assertEquals(stepName, stepExecutionStatusEntity.getStepName());
            assertEquals(status, stepExecutionStatusEntity.getStatus());
            assertEquals(startedAt, stepExecutionStatusEntity.getStartedAt());
            assertEquals(completedAt, stepExecutionStatusEntity.getCompletedAt());
            assertEquals(dependsOnStepIds, stepExecutionStatusEntity.getDependsOnStepIds());
            assertSame(workflowExecutionEntity, stepExecutionStatusEntity.getWorkflowExecutionEntity());
        }
    }

    @Nested
    @DisplayName("toStepExecutionStatus")
    class ToStepExecutionStatusTests {

        @Test
        void toStepExecutionStatus_shouldMapAllFields() {
            UUID id = UUID.randomUUID();
            Integer stepId = 10;
            StepName stepName = null;
            JobStatus status = JobStatus.COMPLETED;
            Instant startedAt = Instant.now().minusSeconds(300);
            Instant completedAt = Instant.now().minusSeconds(10);
            List<Integer> dependsOnStepIds = List.of(4, 5, 6);

            WorkflowExecutionEntity workflowExecutionEntity = WorkflowExecutionEntity.builder()
                    .workflowExecutionId(UUID.randomUUID())
                    .workflowId(UUID.randomUUID())
                    .status(JobStatus.RUNNING)
                    .createdAt(Instant.now().minusSeconds(600))
                    .startedAt(Instant.now().minusSeconds(400))
                    .completedAt(null)
                    .build();

            StepExecutionStatusEntity entity = StepExecutionStatusEntity.builder()
                    .id(id)
                    .workflowExecutionEntity(workflowExecutionEntity)
                    .stepId(stepId)
                    .stepName(stepName)
                    .status(status)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .dependsOnStepIds(dependsOnStepIds)
                    .build();

            StepExecutionStatus statusModel = mapper.toStepExecutionStatus(entity);

            assertEquals(id, statusModel.getId());
            assertEquals(stepId, statusModel.getStepId());
            assertEquals(stepName, statusModel.getStepName());
            assertEquals(status, statusModel.getStatus());
            assertEquals(startedAt, statusModel.getStartedAt());
            assertEquals(completedAt, statusModel.getCompletedAt());
            assertEquals(dependsOnStepIds, statusModel.getDependsOnStepIds());
        }

    }

}

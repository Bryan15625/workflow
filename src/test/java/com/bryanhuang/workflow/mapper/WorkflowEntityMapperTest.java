package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.entity.payload.WorkflowPayload;
import com.bryanhuang.workflow.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class WorkflowEntityMapperTest {

    private final WorkflowEntityMapper mapper = new WorkflowEntityMapper();

    @Nested
    @DisplayName("toWorkflowEntity")
    class ToWorkflowEntityTests {

        @Test
        void toWorkflowEntity_shouldMapAllFields() {
            UUID workflowId = UUID.randomUUID();
            String workflowName = "workflow name";
            Instant createdAt = Instant.now().minusSeconds(60);
            Instant updatedAt = Instant.now();

            Data data = Data.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            List<Step> steps = List.of(Step.builder()
                    .stepId(1)
                    .stepName(StepName.DETECT_ISSUES)
                    .dependsOnStepIds(List.of())
                    .build());

            CohortProfile cohortProfile = CohortProfile.builder()
                    .age(22)
                    .weightKg(75)
                    .heightCm(173)
                    .sex(Sex.MALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            Workflow workflow = Workflow.builder()
                    .workflowId(workflowId)
                    .workflowName(workflowName)
                    .cohortProfile(cohortProfile)
                    .data(data)
                    .steps(steps)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            WorkflowEntity entity = mapper.toWorkflowEntity(workflow);

            assertEquals(workflowId, entity.getWorkflowId());
            assertEquals(workflowName, entity.getWorkflowName());
            assertEquals(createdAt, entity.getCreatedAt());
            assertEquals(updatedAt, entity.getUpdatedAt());
            assertSame(data, entity.getWorkflowJson().getData());
            assertSame(steps, entity.getWorkflowJson().getSteps());
            assertSame(cohortProfile, entity.getWorkflowJson().getCohortProfile());
        }
    }

    @Nested
    @DisplayName("toWorkflow")
    class ToWorkflowTests {

        @Test
        void toWorkflow_shouldMapAllFields() {
            UUID workflowId = UUID.randomUUID();
            String workflowName = "workflow name";

            Data data = Data.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            List<Step> steps = List.of(Step.builder()
                    .stepId(1)
                    .stepName(StepName.DETECT_ISSUES)
                    .dependsOnStepIds(List.of())
                    .build());

            CohortProfile cohortProfile = CohortProfile.builder()
                    .age(22)
                    .weightKg(75)
                    .heightCm(173)
                    .sex(Sex.MALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            WorkflowPayload workflowJson = WorkflowPayload.builder()
                    .cohortProfile(cohortProfile)
                    .data(data)
                    .steps(steps)
                    .build();

            Instant createdAt = Instant.now().minusSeconds(60);
            Instant updatedAt = Instant.now();

            WorkflowEntity entity = WorkflowEntity.builder()
                    .workflowId(workflowId)
                    .workflowName(workflowName)
                    .workflowJson(workflowJson)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            Workflow workflow = mapper.toWorkflow(entity);
            assertEquals(workflowId, workflow.getWorkflowId());
            assertEquals(workflowName, workflow.getWorkflowName());
            assertEquals(createdAt, workflow.getCreatedAt());
            assertEquals(updatedAt, workflow.getUpdatedAt());
            assertSame(data, workflow.getData());
            assertSame(steps, workflow.getSteps());
            assertSame(cohortProfile, workflow.getCohortProfile());
        }

    }
}

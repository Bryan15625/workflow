package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.DataDto;
import com.bryanhuang.workflow.dto.CohortProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.model.*;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.Workflow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkflowMapperTest {

    private final WorkflowMapper mapper = new WorkflowMapper();

    @Nested
    @DisplayName("toWorkflow")
    class ToWorkflowTests {

        @Test
        void toWorkflow_ShouldMapAllFields() {
            UUID workflowId = UUID.randomUUID();
            String workflowName = "Test Workflow";

            CohortProfileDto profile = CohortProfileDto.builder()
                    .age(22)
                    .weightKg(75)
                    .heightCm(173)
                    .sex(Sex.MALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            DataDto data = DataDto.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            List<StepDto> steps = List.of(
                    StepDto.builder()
                            .stepId(1)
                            .stepName(StepName.DETECT_ISSUES)
                            .dependsOnStepIds(List.of())
                            .build()
            );

            CreateWorkflowRequest request = CreateWorkflowRequest.builder()
                    .workflowName(workflowName)
                    .cohortProfile(profile)
                    .data(data)
                    .steps(steps)
                    .build();

            Workflow workflow = mapper.toWorkflow(workflowId, request);

            assertEquals(workflowId, workflow.getWorkflowId());
            assertEquals(workflowName, workflow.getWorkflowName());

            // These fields are not set in the request, so they should be null
            assertNull(workflow.getCreatedAt());
            assertNull(workflow.getUpdatedAt());

            CohortProfile workflowCohortProfile = workflow.getCohortProfile();
            assertEquals(profile.getAge(), workflowCohortProfile.getAge());
            assertEquals(profile.getWeightKg(), workflowCohortProfile.getWeightKg());
            assertEquals(profile.getHeightCm(), workflowCohortProfile.getHeightCm());
            assertEquals(profile.getSex(), workflowCohortProfile.getSex());
            assertEquals(profile.getGoal(), workflowCohortProfile.getGoal());

            Data workflowData = workflow.getData();
            assertEquals(data.getInput(), workflowData.getInput());
            assertEquals(data.getOutput(), workflowData.getOutput());

            List<Step> workflowSteps = workflow.getSteps();
            assertEquals(1, workflowSteps.size());
            assertEquals(steps.getFirst().getStepId(), workflowSteps.getFirst().getStepId());
            assertEquals(steps.getFirst().getStepName(), workflowSteps.getFirst().getStepName());
            assertEquals(steps.getFirst().getDependsOnStepIds(), workflowSteps.getFirst().getDependsOnStepIds());
        }
    }

    @Nested
    @DisplayName("toWorkflowResponse")
    class ToWorkflowResponseTests {

        @Test
        void toWorkflowResponse_shouldMapAllFields() {
            String workflowName = "Test Workflow";
            Instant createdAt = Instant.now().minusSeconds(300);
            Instant updatedAt = Instant.now().minusSeconds(100);

            CohortProfile cohortProfile = CohortProfile.builder()
                    .age(30)
                    .weightKg(80)
                    .heightCm(180)
                    .sex(Sex.MALE)
                    .goal(Goal.MUSCLE_GAIN)
                    .build();

            Data data = Data.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            List<Step> steps = List.of(
                    Step.builder()
                            .stepId(1)
                            .stepName(StepName.DETECT_ISSUES)
                            .dependsOnStepIds(List.of())
                            .build(),
                    Step.builder()
                            .stepId(2)
                            .stepName(StepName.PARSE_CSV)
                            .dependsOnStepIds(List.of(1))
                            .build()
            );

            Workflow workflow = Workflow.builder()
                    .workflowId(UUID.randomUUID())
                    .workflowName(workflowName)
                    .cohortProfile(cohortProfile)
                    .data(data)
                    .steps(steps)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            WorkflowResponse response = mapper.toWorkflowResponse(workflow);

            assertEquals(workflowName, response.getWorkflowName());
            assertEquals(createdAt, response.getCreatedAt());
            assertEquals(updatedAt, response.getUpdatedAt());

            CohortProfileDto cohortProfileDto = response.getCohortProfile();
            assertEquals(cohortProfile.getAge(), cohortProfileDto.getAge());
            assertEquals(cohortProfile.getWeightKg(), cohortProfileDto.getWeightKg());
            assertEquals(cohortProfile.getHeightCm(), cohortProfileDto.getHeightCm());
            assertEquals(cohortProfile.getSex(), cohortProfileDto.getSex());
            assertEquals(cohortProfile.getGoal(), cohortProfileDto.getGoal());

            DataDto dataDto = response.getData();
            assertEquals(data.getInput(), dataDto.getInput());
            assertEquals(data.getOutput(), dataDto.getOutput());

            List<StepDto> stepDtos = response.getSteps();
            assertEquals(2, stepDtos.size());

            StepDto s1 = stepDtos.getFirst();
            assertEquals(steps.getFirst().getStepId(), s1.getStepId());
            assertEquals(steps.getFirst().getStepName(), s1.getStepName());
            assertEquals(steps.getFirst().getDependsOnStepIds(), s1.getDependsOnStepIds());

            StepDto s2 = stepDtos.get(1);
            assertEquals(steps.get(1).getStepId(), s2.getStepId());
            assertEquals(steps.get(1).getStepName(), s2.getStepName());
            assertEquals(steps.get(1).getDependsOnStepIds(), s2.getDependsOnStepIds());
        }
    }

    @Nested
    @DisplayName("toCohortProfile")
    class ToCohortProfileTests {

        @Test
        void toCohortProfile_shouldMapAllFields() {
            CohortProfileDto dto = CohortProfileDto.builder()
                    .age(25)
                    .weightKg(70)
                    .heightCm(175)
                    .sex(Sex.FEMALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            CohortProfile cohortProfile = mapper.toCohortProfile(dto);

            assertEquals(dto.getAge(), cohortProfile.getAge());
            assertEquals(dto.getWeightKg(), cohortProfile.getWeightKg());
            assertEquals(dto.getHeightCm(), cohortProfile.getHeightCm());
            assertEquals(dto.getSex(), cohortProfile.getSex());
            assertEquals(dto.getGoal(), cohortProfile.getGoal());
        }
    }

    @Nested
    @DisplayName("toData")
    class ToDataTests {

        @Test
        void toData_shouldMapAllFields() {
            DataDto dto = DataDto.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            Data data = mapper.toData(dto);

            assertEquals(dto.getInput(), data.getInput());
            assertEquals(dto.getOutput(), data.getOutput());
        }
    }

    @Nested
    @DisplayName("ToSteps")
    class ToStepsTests {

        @Test
        void toSteps_shouldMapAllFields() {
            List<StepDto> stepDtos = List.of(
                    StepDto.builder()
                            .stepId(1)
                            .stepName(StepName.DETECT_ISSUES)
                            .dependsOnStepIds(List.of())
                            .build(),
                    StepDto.builder()
                            .stepId(2)
                            .stepName(StepName.PARSE_CSV)
                            .dependsOnStepIds(List.of(1))
                            .build()
            );

            List<Step> steps = mapper.toSteps(stepDtos);

            assertEquals(2, steps.size());

            Step s1 = steps.getFirst();
            assertEquals(stepDtos.getFirst().getStepId(), s1.getStepId());
            assertEquals(stepDtos.getFirst().getStepName(), s1.getStepName());
            assertEquals(stepDtos.getFirst().getDependsOnStepIds(), s1.getDependsOnStepIds());

            Step s2 = steps.getLast();
            assertEquals(stepDtos.getLast().getStepId(), s2.getStepId());
            assertEquals(stepDtos.getLast().getStepName(), s2.getStepName());
            assertEquals(stepDtos.getLast().getDependsOnStepIds(), s2.getDependsOnStepIds());
        }
    }

    @Nested
    @DisplayName("toCohortProfileDto")
    class ToCohortProfileDtoTests {
        @Test
        void toCohortProfileDto_shouldMapAllFields() {
            CohortProfile cohortProfile = CohortProfile.builder()
                    .age(40)
                    .weightKg(90)
                    .heightCm(185)
                    .sex(Sex.MALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            CohortProfileDto dto = mapper.toCohortProfileDto(cohortProfile);

            assertEquals(cohortProfile.getAge(), dto.getAge());
            assertEquals(cohortProfile.getWeightKg(), dto.getWeightKg());
            assertEquals(cohortProfile.getHeightCm(), dto.getHeightCm());
            assertEquals(cohortProfile.getSex(), dto.getSex());
            assertEquals(cohortProfile.getGoal(), dto.getGoal());
        }
    }

    @Nested
    @DisplayName("toDataDto")
    class ToDataDtoTests {

        @Test
        void toDataDto_shouldMapAllFields() {
            Data data = Data.builder()
                    .input("workout.csv")
                    .output("summary.txt")
                    .build();

            DataDto dto = mapper.toDataDto(data);

            assertEquals(data.getInput(), dto.getInput());
            assertEquals(data.getOutput(), dto.getOutput());
        }
    }

    @Nested
    @DisplayName("toStepDtos")
    class ToStepDtosTests {

        @Test
        void toStepDtos_shouldMapAllFields() {
            List<Step> steps = List.of(
                    Step.builder()
                            .stepId(10)
                            .stepName(StepName.DETECT_ISSUES)
                            .dependsOnStepIds(List.of())
                            .build(),
                    Step.builder()
                            .stepId(20)
                            .stepName(StepName.PARSE_CSV)
                            .dependsOnStepIds(List.of(10))
                            .build()
            );

            List<StepDto> stepDtos = mapper.toStepDtos(steps);

            assertEquals(2, stepDtos.size());

            StepDto s1 = stepDtos.getFirst();
            assertEquals(steps.getFirst().getStepId(), s1.getStepId());
            assertEquals(steps.getFirst().getStepName(), s1.getStepName());
            assertEquals(steps.getFirst().getDependsOnStepIds(), s1.getDependsOnStepIds());

            StepDto s2 = stepDtos.getLast();
            assertEquals(steps.getLast().getStepId(), s2.getStepId());
            assertEquals(steps.getLast().getStepName(), s2.getStepName());
            assertEquals(steps.getLast().getDependsOnStepIds(), s2.getDependsOnStepIds());
        }
    }
}
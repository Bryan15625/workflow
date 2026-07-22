package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.DataDto;
import com.bryanhuang.workflow.dto.ProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.model.*;
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

            ProfileDto profile = ProfileDto.builder()
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
                    .profile(profile)
                    .data(data)
                    .steps(steps)
                    .build();

            Workflow workflow = mapper.toWorkflow(workflowId, request);

            assertEquals(workflowId, workflow.getWorkflowId());
            assertEquals(workflowName, workflow.getWorkflowName());

            // These fields are not set in the request, so they should be null
            assertNull(workflow.getCreatedAt());
            assertNull(workflow.getUpdatedAt());

            Profile workflowProfile = workflow.getProfile();
            assertEquals(profile.getAge(), workflowProfile.getAge());
            assertEquals(profile.getWeightKg(), workflowProfile.getWeightKg());
            assertEquals(profile.getHeightCm(), workflowProfile.getHeightCm());
            assertEquals(profile.getSex(), workflowProfile.getSex());
            assertEquals(profile.getGoal(), workflowProfile.getGoal());

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

            Profile profile = Profile.builder()
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
                    .profile(profile)
                    .data(data)
                    .steps(steps)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();

            WorkflowResponse response = mapper.toWorkflowResponse(workflow);

            assertEquals(workflowName, response.getWorkflowName());
            assertEquals(createdAt, response.getCreatedAt());
            assertEquals(updatedAt, response.getUpdatedAt());

            ProfileDto profileDto = response.getProfileDto();
            assertEquals(profile.getAge(), profileDto.getAge());
            assertEquals(profile.getWeightKg(), profileDto.getWeightKg());
            assertEquals(profile.getHeightCm(), profileDto.getHeightCm());
            assertEquals(profile.getSex(), profileDto.getSex());
            assertEquals(profile.getGoal(), profileDto.getGoal());

            DataDto dataDto = response.getDataDto();
            assertEquals(data.getInput(), dataDto.getInput());
            assertEquals(data.getOutput(), dataDto.getOutput());

            List<StepDto> stepDtos = response.getStepDtos();
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
    @DisplayName("toProfile")
    class ToProfileTests {

        @Test
        void toProfile_shouldMapAllFields() {
            ProfileDto dto = ProfileDto.builder()
                    .age(25)
                    .weightKg(70)
                    .heightCm(175)
                    .sex(Sex.FEMALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            Profile profile = mapper.toProfile(dto);

            assertEquals(dto.getAge(), profile.getAge());
            assertEquals(dto.getWeightKg(), profile.getWeightKg());
            assertEquals(dto.getHeightCm(), profile.getHeightCm());
            assertEquals(dto.getSex(), profile.getSex());
            assertEquals(dto.getGoal(), profile.getGoal());
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
    @DisplayName("toProfileDto")
    class ToProfileDtoTests {
        @Test
        void toProfileDto_shouldMapAllFields() {
            Profile profile = Profile.builder()
                    .age(40)
                    .weightKg(90)
                    .heightCm(185)
                    .sex(Sex.MALE)
                    .goal(Goal.FAT_LOSS)
                    .build();

            ProfileDto dto = mapper.toProfileDto(profile);

            assertEquals(profile.getAge(), dto.getAge());
            assertEquals(profile.getWeightKg(), dto.getWeightKg());
            assertEquals(profile.getHeightCm(), dto.getHeightCm());
            assertEquals(profile.getSex(), dto.getSex());
            assertEquals(profile.getGoal(), dto.getGoal());
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
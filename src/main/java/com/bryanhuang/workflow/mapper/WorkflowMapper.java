package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.InputDto;
import com.bryanhuang.workflow.dto.ProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.model.Input;
import com.bryanhuang.workflow.model.Profile;
import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WorkflowMapper {

    public Workflow toWorkflow(UUID workflowId, CreateWorkflowRequest request) {

        return Workflow.builder()
                .workflowId(workflowId)
                .workflowName(request.getWorkflowName())
                .profile(toProfile(request.getProfile()))
                .input(toInput(request.getInput()))
                .steps(toSteps(request.getSteps()))
                .build();
    }

    public WorkflowResponse toWorkflowResponse(Workflow workflow) {

        return WorkflowResponse.builder()
                .workflowName(workflow.getWorkflowName())
                .profileDto(toProfileDto(workflow.getProfile()))
                .inputDto(toInputDto(workflow.getInput()))
                .stepDtos(toStepDtos(workflow.getSteps()))
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }

    public Profile toProfile(ProfileDto profileDto) {

        return Profile.builder()
                .age(profileDto.getAge())
                .weightKg(profileDto.getWeightKg())
                .heightCm(profileDto.getHeightCm())
                .sex(profileDto.getSex())
                .goal(profileDto.getGoal())
                .build();
    }

    public Input toInput(InputDto inputDto) {

        return Input.builder()
                .sourceFilePath(inputDto.getSourceFilePath())
                .resultFilePath(inputDto.getResultFilePath())
                .build();
    }

    public List<Step> toSteps(List<StepDto> stepDtos) {

        return stepDtos.stream()
            .map(step -> Step.builder()
                    .stepId(step.getStepId())
                    .stepName(step.getStepName())
                    .dependsOnStepIds(step.getDependsOnStepIds())
                    .build()
            )
            .toList();
    }

    public ProfileDto toProfileDto(Profile profile) {

        return ProfileDto.builder()
                .age(profile.getAge())
                .weightKg(profile.getWeightKg())
                .heightCm(profile.getHeightCm())
                .sex(profile.getSex())
                .goal(profile.getGoal())
                .build();
    }

    public InputDto toInputDto(Input input) {

        return InputDto.builder()
                .sourceFilePath(input.getSourceFilePath())
                .resultFilePath(input.getResultFilePath())
                .build();
    }

    public List<StepDto> toStepDtos(List<Step> steps) {

        return steps.stream()
                .map(step -> StepDto.builder()
                        .stepId(step.getStepId())
                        .stepName(step.getStepName())
                        .dependsOnStepIds(step.getDependsOnStepIds())
                        .build()
                ).toList();
    }
}

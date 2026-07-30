package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.DataDto;
import com.bryanhuang.workflow.dto.CohortProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class WorkflowMapper {

    public Workflow toWorkflow(UUID workflowId, CreateWorkflowRequest request) {

        return Workflow.builder()
                .workflowId(workflowId)
                .workflowName(request.getWorkflowName())
                .cohortProfile(toCohortProfile(request.getCohortProfile()))
                .data(toData(request.getData()))
                .steps(toSteps(request.getSteps()))
                .build();
    }

    public WorkflowResponse toWorkflowResponse(Workflow workflow) {

        return WorkflowResponse.builder()
                .workflowName(workflow.getWorkflowName())
                .cohortProfile(toCohortProfileDto(workflow.getCohortProfile()))
                .data(toDataDto(workflow.getData()))
                .steps(toStepDtos(workflow.getSteps()))
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }

    public CohortProfile toCohortProfile(CohortProfileDto cohortProfileDto) {

        return CohortProfile.builder()
                .age(cohortProfileDto.getAge())
                .weightKg(cohortProfileDto.getWeightKg())
                .heightCm(cohortProfileDto.getHeightCm())
                .sex(cohortProfileDto.getSex())
                .goal(cohortProfileDto.getGoal())
                .build();
    }

    public Data toData(DataDto dataDto) {

        return Data.builder()
                .input(dataDto.getInput())
                .output(dataDto.getOutput())
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

    public CohortProfileDto toCohortProfileDto(CohortProfile cohortProfile) {

        return CohortProfileDto.builder()
                .age(cohortProfile.getAge())
                .weightKg(cohortProfile.getWeightKg())
                .heightCm(cohortProfile.getHeightCm())
                .sex(cohortProfile.getSex())
                .goal(cohortProfile.getGoal())
                .build();
    }

    public DataDto toDataDto(Data data) {

        return DataDto.builder()
                .input(data.getInput())
                .output(data.getOutput())
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

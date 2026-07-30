package com.bryanhuang.workflow.dto.request;

import com.bryanhuang.workflow.dto.DataDto;
import com.bryanhuang.workflow.dto.CohortProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class CreateWorkflowRequest {
    @NotBlank(message = "Workflow name is required and cannot be blank")
    private String workflowName;

    @NotNull(message = "Cohort Profile is required")
    @Valid
    private CohortProfileDto cohortProfile;

    @NotNull(message = "Data is required")
    @Valid
    private DataDto data;

    @Valid
    @NotEmpty(message = "At least one step is required")
    private List<StepDto> steps;

    public CreateWorkflowRequest(
            String workflowName,
            CohortProfileDto cohortProfile,
            DataDto data,
            List<StepDto> steps
    ) {
        this.workflowName = workflowName;
        this.cohortProfile = cohortProfile;
        this.data = data;
        this.steps = steps;
    }
}

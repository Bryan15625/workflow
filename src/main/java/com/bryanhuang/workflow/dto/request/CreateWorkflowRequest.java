package com.bryanhuang.workflow.dto.request;

import com.bryanhuang.workflow.dto.InputDto;
import com.bryanhuang.workflow.dto.ProfileDto;
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

    @NotNull(message = "Profile is required")
    @Valid
    private ProfileDto profile;

    @NotNull(message = "Input is required")
    @Valid
    private InputDto input;

    @Valid
    @NotEmpty(message = "At least one step is required")
    private List<StepDto> steps;

    public CreateWorkflowRequest(
            String workflowName,
            ProfileDto profile,
            InputDto input,
            List<StepDto> steps
    ) {
        this.workflowName = workflowName;
        this.profile = profile;
        this.input = input;
        this.steps = steps;
    }
}

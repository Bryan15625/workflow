package com.bryanhuang.workflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateWorkflowDefinitionRequest {
    @NotBlank(message = "Workflow definition name is required and cannot be blank")
    private String workflowDefinitionName;

    @NotNull(message = "Profile is required")
    @Valid
    private ProfileRequest profile;

    @NotNull(message = "Input is required")
    @Valid
    private InputRequest input;

    @Valid
    @NotEmpty(message = "At least one definition step is required")
    private List<StepRequest> steps;
}

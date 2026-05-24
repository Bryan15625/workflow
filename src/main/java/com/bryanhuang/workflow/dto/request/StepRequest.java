package com.bryanhuang.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StepRequest {
    @NotNull(message = "Step ID is required and must be an Integer")
    private Integer stepId;

    @NotBlank(message = "Step name is required and cannot be blank")
    private String stepName;

    @NotNull(message = "Next step IDs are required and must not be empty")
    private List<Integer> nextStepIds;
}

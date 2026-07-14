package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.dto.InputDto;
import com.bryanhuang.workflow.dto.ProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import lombok.*;

import java.time.Instant;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
public class WorkflowResponse {
    private String workflowName;
    private ProfileDto profileDto;
    private InputDto inputDto;
    private List<StepDto> stepDtos;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkflowResponse(
            String workflowName,
            ProfileDto profileDto,
            InputDto inputDto,
            List<StepDto> stepDtos,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.workflowName = workflowName;
        this.profileDto = profileDto;
        this.inputDto = inputDto;
        this.stepDtos = stepDtos;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

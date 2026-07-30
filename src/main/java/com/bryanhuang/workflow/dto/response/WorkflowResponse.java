package com.bryanhuang.workflow.dto.response;

import com.bryanhuang.workflow.dto.DataDto;
import com.bryanhuang.workflow.dto.CohortProfileDto;
import com.bryanhuang.workflow.dto.StepDto;
import lombok.*;

import java.time.Instant;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
public class WorkflowResponse {
    private String workflowName;
    private CohortProfileDto cohortProfile;
    private DataDto data;
    private List<StepDto> steps;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkflowResponse(
            String workflowName,
            CohortProfileDto cohortProfile,
            DataDto data,
            List<StepDto> steps,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.workflowName = workflowName;
        this.cohortProfile = cohortProfile;
        this.data = data;
        this.steps = steps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
